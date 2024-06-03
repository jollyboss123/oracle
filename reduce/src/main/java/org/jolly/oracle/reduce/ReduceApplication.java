package org.jolly.oracle.reduce;

import org.jolly.oracle.reduce.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {ApplicationProperties.class})
public class ReduceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReduceApplication.class, args);
    }

}
