package org.jolly.oracle.map.config.scheduler;

import org.jolly.oracle.map.service.scheduled.SchedulerManager;

@FunctionalInterface
public interface SchedulerManagerCustomizer {
    void customize(SchedulerManager schedulerManager);
}
