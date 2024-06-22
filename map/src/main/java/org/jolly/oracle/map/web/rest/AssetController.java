package org.jolly.oracle.map.web.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.service.scheduled.AssetTickerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/oracle")
@RequiredArgsConstructor
@Slf4j
public class AssetController {
    private final AssetTickerService assetTickerService;

    @GetMapping("/cron")
    ResponseEntity<Void> run() {
        assetTickerService.fetchStocks();
        return ResponseEntity.ok().build();
    }
}
