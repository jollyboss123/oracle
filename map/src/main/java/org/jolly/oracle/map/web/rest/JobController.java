package org.jolly.oracle.map.web.rest;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;

@RestController
@RequestMapping("oracle/job")
@Validated
@RequiredArgsConstructor
@Slf4j
public class JobController {
    private final Scheduler scheduler;
    @Qualifier("fetchStocksInfoJobDetail")
    private final JobDetail fetchStocksInfoJobDetail;

    @Value
    @Builder
    @Jacksonized
    public static class ScheduleJobRequest {
        String jobName;
        String cronExpression;
    }

    @PostMapping(value = "schedule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> schedule(@RequestBody ScheduleJobRequest request) throws SchedulerException {
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .forJob(fetchStocksInfoJobDetail)
                .withSchedule(CronScheduleBuilder.cronSchedule(request.getCronExpression()))
                .withIdentity(request.getJobName() + "-" + LocalDateTime.now())
                .startNow()
                .build();

        Date date = scheduler.scheduleJob(fetchStocksInfoJobDetail, cronTrigger);
        log.info("Job is created. It will be triggered at {}", date);

        return ResponseEntity.ok().build();
    }

}
