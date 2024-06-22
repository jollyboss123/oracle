package org.jolly.oracle.map.config;

import org.jolly.oracle.map.service.scheduled.FetchStocksInfoJob;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
public class JobConfiguration {

    @Bean
    public JobDetailFactoryBean fetchStocksInfoJobDetail() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(FetchStocksInfoJob.class);
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }
}
