package org.jolly.oracle.map.service.scheduled;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.*;
import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Profile("scheduling")
@Component
@Slf4j
@RequiredArgsConstructor
public class SchedulerManager {
    private final Map<String, ScheduledFutureHolder> scheduledFutures = new ConcurrentHashMap<>();
    private final TaskScheduler taskScheduler;
    private final LockProvider lockProvider;

    @Value
    @Builder
    private static class ScheduledFutureHolder {
        ScheduledFuture<?> scheduledFuture;
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
        LockableTaskScheduler lockableTaskScheduler = lockableTaskScheduler(taskScheduler,
                lockProvider, jobName, lockAtMostFor, lockAtLeastFor);
        ScheduledFuture<?> scheduledFuture = lockableTaskScheduler.schedule(task, cronTrigger);

        scheduledFutures.put(jobName,
                ScheduledFutureHolder.builder()
                        .scheduledFuture(scheduledFuture)
                        .task(task)
                        .lockAtMostFor(lockAtMostFor)
                        .lockAtLeastFor(lockAtLeastFor)
                .build());
        log.info("scheduled job: {} with cron: {}", jobName, cronExpression);
    }

    public void rescheduleJob(String jobName, String cronExpression) throws TaskNotFoundException {
        if (!taskExists(jobName)) {
            throw new TaskNotFoundException(jobName);
        }
        log.info("rescheduling job: {}", jobName);

        ScheduledFutureHolder holder = scheduledFutures.get(jobName);
        scheduleJob(jobName, holder.getTask(), cronExpression, holder.getLockAtMostFor(), holder.getLockAtLeastFor());
    }

    public void cancelJob(String jobName, boolean mayInterruptIfRunning) throws TaskNotFoundException {
        if (!taskExists(jobName)) {
            throw new TaskNotFoundException(jobName);
        }
        log.info("cancelling job: {}", jobName);

        ScheduledFuture<?> scheduledFuture = scheduledFutures.get(jobName).getScheduledFuture();
        if (scheduledFuture != null && !scheduledFuture.isDone() && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(mayInterruptIfRunning);
            scheduledFutures.remove(jobName);
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
