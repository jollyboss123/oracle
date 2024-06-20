package org.jolly.oracle.map.config;

import org.jolly.oracle.map.service.scheduled.AssetTickerService;
import org.jolly.oracle.map.service.SchedulerErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

//TODO: implement retry
@Profile("scheduling")
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    private final ThreadPoolTaskScheduler taskScheduler;
    private final AssetTickerService assetTickerService;

    SchedulerConfig(SchedulerErrorHandler schedulerErrorHandler,
                    AssetTickerService assetTickerService) {
        this.assetTickerService = assetTickerService;

        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setErrorHandler(schedulerErrorHandler);

        taskScheduler.initialize();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler);
    }

    @Bean
    @Scheduled(cron = "0 0 * * * ?")
    public void fetchStocksScheduler() {
        assetTickerService.fetchStocks();
    }
}
