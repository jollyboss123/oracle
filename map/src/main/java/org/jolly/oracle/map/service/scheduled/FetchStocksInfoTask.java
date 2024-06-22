package org.jolly.oracle.map.service.scheduled;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FetchStocksInfoTask implements Runnable {
    public static final String TASK_NAME = "fetch-stocks-info";

    @Override
    public void run() {
        log.info("hello");
    }
}
