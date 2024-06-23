package org.jolly.oracle.map.web.rest;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.service.scheduled.AssetTickerService;
import org.jolly.oracle.map.service.scheduled.FetchStocksInfoTask;
import org.jolly.oracle.map.service.scheduled.SchedulerManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/oracle")
@RequiredArgsConstructor
@Slf4j
public class AssetController {
    private final AssetTickerService assetTickerService;
    private final SchedulerManager schedulerManager;
    private final FetchStocksInfoTask fetchStocksInfoTask;

    @GetMapping("/cron")
    ResponseEntity<Void> run() {
        assetTickerService.fetchStocks();
        return ResponseEntity.ok().build();
    }

    @Value
    @Builder
    @Jacksonized
    public static class ScheduleTaskRequest {
        String taskName;
        String cronExpression;
    }

    @PostMapping(value = "/cron/schedule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> schedule(@RequestBody ScheduleTaskRequest request) {
        if (FetchStocksInfoTask.TASK_NAME.equals(request.getTaskName())) {
            schedulerManager.scheduleTask(request.getTaskName(), fetchStocksInfoTask, request.getCronExpression(), Duration.ofMinutes(5), Duration.ofSeconds(20));
        }
        return ResponseEntity.ok().build();
    }
}
