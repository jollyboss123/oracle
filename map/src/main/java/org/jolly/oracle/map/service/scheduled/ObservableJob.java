package org.jolly.oracle.map.service.scheduled;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public final class ObservableJob implements Runnable {
    private final Runnable task;
    private final String jobName;
    private final Consumer<JobStatusEvent> statusEmitter;

    public ObservableJob(String jobName, Runnable task, Consumer<JobStatusEvent> statusEmitter) {
        this.task = Objects.requireNonNull(task);
        this.jobName = jobName;
        this.statusEmitter = statusEmitter;
    }

    @Override
    public void run() {
        statusEmitter.accept(
                JobStatusEvent.builder()
                        .jobName(jobName)
                        .status(JobStatus.RUNNING)
                        .build()
        );
        try {
            task.run();
        } catch (Exception e) {
            statusEmitter.accept(
                    JobStatusEvent.builder()
                            .jobName(jobName)
                            .status(JobStatus.EXCEPTION)
                            .exceptionMessage(e.getMessage())
                            .build()
            );
        } finally {
            statusEmitter.accept(
                    JobStatusEvent.builder()
                            .jobName(jobName)
                            .status(JobStatus.COMPLETED)
                            .build()
            );
        }
    }
}
