package org.jolly.oracle.map.config.scheduler;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.jolly.oracle.map.domain.JobDetail;
import org.jolly.oracle.map.repository.JobDetailRepository;
import org.jolly.oracle.map.service.SchedulerErrorHandler;
import org.jolly.oracle.map.service.scheduled.JobDetailService;
import org.jolly.oracle.map.service.scheduled.JobTriggerService;
import org.jolly.oracle.map.service.scheduled.SchedulerManager;
import org.jolly.oracle.map.service.scheduled.job.FetchStocksInfoJob;
import org.jolly.oracle.map.service.scheduled.job.HelloJob;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.util.List;

//TODO: implement retry
@Profile("scheduling")
@Configuration
@RequiredArgsConstructor
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class SchedulerConfiguration implements SchedulingConfigurer {
    private final SchedulerErrorHandler errorHandler;

    @Bean
    public LockProvider lockProvider(RedissonConnectionFactory redissonConnectionFactory,
                                     @Value("{spring.profiles.active}") String environment) {
        return new RedisLockProvider(redissonConnectionFactory, environment);
    }

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
    public SchedulerManager schedulerManager(List<SchedulerManagerCustomizer> customizers,
                                             ThreadPoolTaskScheduler taskScheduler,
                                             LockProvider lockProvider,
                                             JobDetailService jobDetailService,
                                             JobTriggerService jobTriggerService) {
         SchedulerManager schedulerManager = new SchedulerManager(taskScheduler, lockProvider, jobDetailService, jobTriggerService);
         customizers.forEach(customizer -> customizer.customize(schedulerManager));
         return schedulerManager;
    }

    @Bean
    public SchedulerManagerCustomizer fetchStocksInfoJobCustomizer(FetchStocksInfoJob fetchStocksInfoJob,
                                                                   JobDetailRepository jobDetailRepository) {
        return manager -> manager.scheduleJob(
                FetchStocksInfoJob.JOB_NAME,
                fetchStocksInfoJob,
                jobDetailRepository.findByName(FetchStocksInfoJob.JOB_NAME)
                        .map(JobDetail::getCronExpression)
                        .orElse("0 0 * * * ?"),
                Duration.ofMinutes(5),
                Duration.ofMinutes(2)
                );
    }

    @Override
    public synchronized void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
        taskRegistrar.afterPropertiesSet();
    }
}
