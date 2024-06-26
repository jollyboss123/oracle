package org.jolly.oracle.map.web.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.service.scheduled.JobDetailService;
import org.jolly.oracle.map.service.scheduled.SchedulerManager;
import org.jolly.oracle.map.service.scheduled.JobNotFoundException;
import org.jolly.oracle.map.service.scheduled.job.FetchStocksInfoJob;
import org.jolly.oracle.map.web.rest.dto.JobDetailResponse;
import org.jolly.oracle.map.web.rest.dto.ScheduleJobRequest;
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
    private final JobDetailService jobDetailService;

    @PostMapping(value = "/schedule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> schedule(@RequestBody ScheduleJobRequest request) {
        try {
            schedulerManager.rescheduleJob(request.getJobName(), request.getCronExpression());
            return ResponseEntity.created(URI.create("/oracle/job/schedule/" + request.getJobName())).build();
        } catch (JobNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{jobName}")
    ResponseEntity<Void> cancel(@PathVariable("jobName") String jobName) {
        try {
            schedulerManager.cancelJob(jobName, true);
            return ResponseEntity.ok().build();
        } catch (JobNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{jobName}")
    ResponseEntity<JobDetailResponse> getDetails(@PathVariable("jobName") String jobName) {
        return jobDetailService.getDetails(jobName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/run/stocks")
    ResponseEntity<Void> fetchStocksInfo() {
        fetchStocksInfoJob.run();
        return ResponseEntity.ok().build();
    }
}
