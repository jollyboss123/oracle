package org.jolly.oracle.map.web.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.service.scheduled.JobDetailService;
import org.jolly.oracle.map.service.scheduled.JobStatus;
import org.jolly.oracle.map.service.scheduled.job.FetchStocksInfoJob;
import org.jolly.oracle.map.service.scheduled.SchedulerManager;
import org.jolly.oracle.map.service.scheduled.TaskNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@Profile("scheduling")
@RestController
@RequestMapping("/oracle/job")
@RequiredArgsConstructor
@Slf4j
public class JobController {
    private final SchedulerManager schedulerManager;
    private final FetchStocksInfoJob fetchStocksInfoJob;
    private final JobDetailService jobDetailService;

    @Value
    @Builder
    @Jacksonized
    public static class ScheduleJobRequest {
        @NotNull
        @NotBlank
        String jobName;
        @NotNull
        @NotBlank
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

    @DeleteMapping("/{jobName}")
    ResponseEntity<Void> cancel(@PathVariable("jobName") String jobName) {
        try {
            schedulerManager.cancelJob(jobName, true);
            return ResponseEntity.ok().build();
        } catch (TaskNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Value
    @Builder
    @Jacksonized
    public static class JobDetailResponse {
        String name;
        String cronExpression;
        JobStatus latestStatus;
        LocalDateTime prevFireTime;
    }

    @GetMapping("/{jobName}")
    ResponseEntity<JobDetailResponse> getDetails(@PathVariable("jobName") String jobName) {
        return jobDetailService.getDetails(jobName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stocks")
    ResponseEntity<Void> fetchStocksInfo() {
        fetchStocksInfoJob.run();
        return ResponseEntity.ok().build();
    }
}
