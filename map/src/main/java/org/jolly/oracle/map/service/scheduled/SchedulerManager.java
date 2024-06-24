package org.jolly.oracle.map.service.scheduled;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.*;
import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import org.jolly.oracle.map.domain.JobDetail;
import org.jolly.oracle.map.domain.JobTrigger;
import org.jolly.oracle.map.service.scheduled.job.ObservableJob;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

//TODO: make job async
@Profile("scheduling")
@Component
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchedulerManager {
    private final Map<String, ScheduledFutureHolder> scheduledFutures = new ConcurrentHashMap<>();
    private final TaskScheduler taskScheduler;
    private final LockProvider lockProvider;
    private final JobDetailService jobDetailService;
    private final JobTriggerService jobTriggerService;

    @Value
    @Builder
    private static class ScheduledFutureHolder {
        Future<?> scheduledFuture;
        Runnable task;
        Duration lockAtMostFor;
        Duration lockAtLeastFor;
    }

    public void scheduleJob(String jobName, Runnable task, String cronExpression, Duration lockAtMostFor, Duration lockAtLeastFor) {
        log.info("scheduling job: {}", jobName);
        if (taskExists(jobName)) {
            try {
                cancelJob(jobName, false);
            } catch (TaskNotFoundException ignored) {
                // should not reach here
            }
        }

        CronTrigger cronTrigger = new CronTrigger(cronExpression);
        JobDetail jobDetail = jobDetailService.findByName(jobName)
                .orElseGet(() -> new JobDetail()
                        .setName(jobName))
                .setIsActive(true)
                .setCronExpression(cronExpression);

        LockableTaskScheduler lockableTaskScheduler = lockableTaskScheduler(taskScheduler,
                lockProvider, jobName, lockAtMostFor, lockAtLeastFor);
        CancellableFuture<?> scheduledFuture = new CancellableFuture<>(
                lockableTaskScheduler.schedule(
                        new ObservableJob(task, event -> CompletableFuture.runAsync(() -> {
                            log.info("job-{} :: {}", jobName, event);
                            jobTriggerService.save(new JobTrigger()
                                    .setName(jobName)
                                    .setStatus(event)
                                    .setDetail(jobDetail));
                        })
                        .exceptionallyAsync(cause -> {
                            log.error("error persisting job: {}", jobName, cause);
                            return null;
                        })),
                        cronTrigger),
                () -> CompletableFuture.runAsync(() -> {
                    log.info("job-{} :: {}", jobName, JobStatus.CANCELLED);
                    jobTriggerService.save(new JobTrigger()
                            .setName(jobName)
                            .setStatus(JobStatus.CANCELLED)
                            .setDetail(jobDetail));
                })
                .exceptionallyAsync(cause -> {
                    log.error("error persisting cancelled job: {}", jobName, cause);
                    return null;
                })
        );

        scheduledFutures.put(jobName,
                ScheduledFutureHolder.builder()
                        .scheduledFuture(scheduledFuture)
                        .task(task)
                        .lockAtMostFor(lockAtMostFor)
                        .lockAtLeastFor(lockAtLeastFor)
                .build());
        jobDetailService.save(jobDetail);
        log.info("scheduled job: {} with cron: {}", jobName, cronExpression);
    }

    public void rescheduleJob(String jobName, String cronExpression) throws TaskNotFoundException {
        log.info("rescheduling job: {}", jobName);

        ScheduledFutureHolder holder = scheduledFutures.get(jobName);
        scheduleJob(jobName, holder.getTask(), cronExpression, holder.getLockAtMostFor(), holder.getLockAtLeastFor());
    }

    public void cancelJob(String jobName, boolean mayInterruptIfRunning) throws TaskNotFoundException {
        if (!taskExists(jobName)) {
            throw new TaskNotFoundException(jobName);
        }
        log.info("cancelling job: {}", jobName);

        Future<?> scheduledFuture = scheduledFutures.get(jobName).getScheduledFuture();
        if (scheduledFuture != null && !scheduledFuture.isDone() && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(mayInterruptIfRunning);
            JobDetail jobDetail = jobDetailService.findByName(jobName)
                            .orElseThrow(() -> new IllegalStateException("Job: " + jobName + " should exist"));
            jobDetail.setIsActive(false);
            log.info("cancelled job: {}", jobName);
        }
    }

    private boolean taskExists(String taskName) {
        return scheduledFutures.containsKey(taskName);
    }

    private static LockableTaskScheduler lockableTaskScheduler(TaskScheduler taskScheduler,
                                                                LockProvider lockProvider,
                                                                String jobName,
                                                                Duration lockAtMostFor,
                                                                Duration lockAtLeastFor) {
        LockingTaskExecutor taskExecutor = new DefaultLockingTaskExecutor(lockProvider);
        LockConfigurationExtractor configurationExtractor = __ -> Optional.of(new LockConfiguration(
                Instant.now(),
                jobName + "-" + "scheduler-lock",
                lockAtMostFor,
                lockAtLeastFor
        ));
        LockManager manager = new DefaultLockManager(taskExecutor, configurationExtractor);
        return new LockableTaskScheduler(taskScheduler, manager);
    }
}
