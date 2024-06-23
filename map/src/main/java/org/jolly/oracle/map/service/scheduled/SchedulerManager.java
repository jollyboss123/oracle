package org.jolly.oracle.map.service.scheduled;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.*;
import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

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

    public void scheduleTask(String taskName, Runnable task, String cronExpression, Duration lockAtMostFor, Duration lockAtLeastFor) {
        log.info("scheduling task: {}", taskName);
        ScheduledFuture<?> scheduledFuture;
        if (taskExists(taskName)) {
            scheduledFuture = scheduledFutures.get(taskName).getScheduledFuture();
            if (scheduledFuture != null && !scheduledFuture.isDone() && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(false);
                log.info("cancelled current scheduled task: {}", taskName);
            }
        }

        CronTrigger cronTrigger = new CronTrigger(cronExpression);
        LockableTaskScheduler lockableTaskScheduler = lockableTaskScheduler(taskScheduler,
                lockProvider, taskName, lockAtMostFor, lockAtLeastFor);
        scheduledFuture = lockableTaskScheduler.schedule(task, cronTrigger);

        scheduledFutures.put(taskName, ScheduledFutureHolder.builder()
                        .scheduledFuture(scheduledFuture)
                        .task(task)
                        .lockAtMostFor(lockAtMostFor)
                        .lockAtLeastFor(lockAtLeastFor)
                .build());
        log.info("scheduled task: {} with cron: {}", taskName, cronExpression);
    }

    public void rescheduleTask(String taskName, String cronExpression) throws TaskNotFoundException {
        if (!taskExists(taskName)) {
            throw new TaskNotFoundException(taskName);
        }
        log.info("rescheduling task: {}", taskName);

        ScheduledFutureHolder holder = scheduledFutures.get(taskName);
        scheduleTask(taskName, holder.getTask(), cronExpression, holder.getLockAtMostFor(), holder.getLockAtLeastFor());
    }

    private boolean taskExists(String taskName) {
        return scheduledFutures.containsKey(taskName);
    }

    private static LockableTaskScheduler lockableTaskScheduler(TaskScheduler taskScheduler,
                                                                LockProvider lockProvider,
                                                                String taskName,
                                                                Duration lockAtMostFor,
                                                                Duration lockAtLeastFor) {
        LockingTaskExecutor taskExecutor = new DefaultLockingTaskExecutor(lockProvider);
        LockConfigurationExtractor configurationExtractor = __ -> Optional.of(new LockConfiguration(
                Instant.now(),
                taskName + "-" + "scheduler-lock",
                lockAtMostFor,
                lockAtLeastFor
        ));
        LockManager manager = new DefaultLockManager(taskExecutor, configurationExtractor);
        return new LockableTaskScheduler(taskScheduler, manager);
    }
}
