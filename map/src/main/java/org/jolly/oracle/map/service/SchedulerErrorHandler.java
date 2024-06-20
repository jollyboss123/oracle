package org.jolly.oracle.map.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Component
@Slf4j
public class SchedulerErrorHandler implements ErrorHandler {

    @Override
    public void handleError(Throwable t) {
        log.error("error encountered in scheduled task: {}", t.getMessage());
        //TODO: implement alerting system with grafana
    }
}
