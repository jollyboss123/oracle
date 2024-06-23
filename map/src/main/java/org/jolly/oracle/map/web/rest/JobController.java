package org.jolly.oracle.map.web.rest;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.service.scheduled.SchedulerManager;
import org.jolly.oracle.map.service.scheduled.TaskNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/oracle/job")
@RequiredArgsConstructor
@Slf4j
public class JobController {
    private final SchedulerManager schedulerManager;

    @Value
    @Builder
    @Jacksonized
    public static class ScheduleTaskRequest {
        String taskName;
        String cronExpression;
    }

    @PostMapping(value = "/schedule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> schedule(@RequestBody ScheduleTaskRequest request) {
        try {
            schedulerManager.rescheduleTask(request.getTaskName(), request.getCronExpression());
            return ResponseEntity.created(URI.create("/oracle/job/schedule/" + request.getTaskName())).build();
        } catch (TaskNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
