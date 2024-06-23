package org.jolly.oracle.map.web.rest;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.service.scheduled.FetchStocksInfoJob;
import org.jolly.oracle.map.service.scheduled.SchedulerManager;
import org.jolly.oracle.map.service.scheduled.TaskNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Profile("scheduling")
@RestController
@RequestMapping("/oracle/job")
@RequiredArgsConstructor
@Slf4j
public class JobController {
    private final SchedulerManager schedulerManager;
    private final FetchStocksInfoJob fetchStocksInfoJob;

    @Value
    @Builder
    @Jacksonized
    public static class ScheduleJobRequest {
        String jobName;
        String cronExpression;
    }

    @PostMapping(value = "/schedule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> schedule(@RequestBody ScheduleJobRequest request) {
        try {
            schedulerManager.rescheduleJob(request.getJobName(), request.getCronExpression());
            return ResponseEntity.created(URI.create("/oracle/job/schedule/" + request.getJobName())).build();
        } catch (TaskNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/cancel/{jobName}")
    ResponseEntity<Void> cancel(@PathVariable("jobName") String jobName) {
        try {
            schedulerManager.cancelJob(jobName, true);
            return ResponseEntity.ok().build();
        } catch (TaskNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stocks")
    ResponseEntity<Void> fetchStocksInfo() {
        fetchStocksInfoJob.run();
        return ResponseEntity.ok().build();
    }
}
