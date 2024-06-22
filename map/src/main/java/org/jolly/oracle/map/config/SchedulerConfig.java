package org.jolly.oracle.map.config;

import lombok.RequiredArgsConstructor;
import org.jolly.oracle.map.service.scheduled.AssetTickerService;
import org.jolly.oracle.map.service.SchedulerErrorHandler;
import org.jolly.oracle.map.service.scheduled.FetchStocksInfoTask;
import org.jolly.oracle.map.service.scheduled.SchedulerManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

//TODO: implement retry
//@Profile("scheduling")
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer, DisposableBean {
    private final SchedulerErrorHandler errorHandler;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("oracle-map-scheduling");
        scheduler.setErrorHandler(errorHandler);
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();

        return scheduler;
    }

    @Bean
    public SchedulerManager schedulerManager(SchedulerManagerCustomizer customizer, ThreadPoolTaskScheduler taskScheduler) {
        return new SchedulerManager(taskScheduler, customizer);

    }

    @Bean
    public SchedulerManagerCustomizer taskCustomizers(FetchStocksInfoTask fetchStocksInfoTask) {
        return manager -> manager.scheduleTask(FetchStocksInfoTask.TASK_NAME, fetchStocksInfoTask, "*/10 * * * * *");
    }

    @Override
    public synchronized void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
        taskRegistrar.afterPropertiesSet();
    }

    @Override
    public void destroy() {
        taskScheduler().shutdown();
    }
}
