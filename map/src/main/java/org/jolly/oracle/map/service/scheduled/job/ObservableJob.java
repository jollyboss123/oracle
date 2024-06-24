package org.jolly.oracle.map.service.scheduled.job;

import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.service.scheduled.JobStatus;
import org.springframework.scheduling.annotation.Async;

import java.util.Objects;
import java.util.function.Consumer;

@Async
@Slf4j
public class ObservableJob implements Runnable {
    private final Runnable task;
    private final Consumer<JobStatus> statusEmitter;

    public ObservableJob(Runnable task, Consumer<JobStatus> statusEmitter) {
        this.task = Objects.requireNonNull(task);
        this.statusEmitter = statusEmitter;
    }

    @Override
    public void run() {
        statusEmitter.accept(JobStatus.RUNNING);
        try {
            task.run();
        } catch (Exception e) {
            statusEmitter.accept(JobStatus.EXCEPTION);
        } finally {
            statusEmitter.accept(JobStatus.COMPLETED);
        }
    }
}
