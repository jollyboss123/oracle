package org.jolly.oracle.map.config;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class QuartzConfiguration {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, Properties quartzProperties) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(new SpringBeanJobFactory());
        factory.setDataSource(dataSource);
        factory.setQuartzProperties(quartzProperties);
        factory.setOverwriteExistingJobs(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);
        return factory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean factory = new PropertiesFactoryBean();
        factory.setLocation(new ClassPathResource("/quartz.properties"));
        factory.afterPropertiesSet();

        return factory.getObject();
    }
}
