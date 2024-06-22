package org.jolly.oracle.map.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class SchedulerManager {
    private final Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();
    private final ThreadPoolTaskScheduler taskScheduler;

    public void scheduleTask(String taskName, Runnable task, String cronExpression) {
        ScheduledFuture<?> scheduledFuture = scheduledFutures.get(taskName);
        if (scheduledFuture != null && !scheduledFuture.isDone() && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false);
            log.info("cancelled current scheduled task");
        }

        CronTrigger cronTrigger = new CronTrigger(cronExpression);
        scheduledFuture = taskScheduler.schedule(task, cronTrigger);

        scheduledFutures.put(taskName, scheduledFuture);
    }
}
