package org.jolly.oracle.map.config;

import org.jolly.oracle.map.service.scheduled.SchedulerManager;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@FunctionalInterface
public interface SchedulerManagerCustomizer {
    void customize(SchedulerManager schedulerManager);
}
