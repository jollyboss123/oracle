package org.jolly.oracle.reduce.broker;

import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.reduce.service.QuotesMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class QuotesConsumer {

    @Bean
    public Consumer<QuotesMessage> quotes() {
        return quotes -> log.info("received: {}", Arrays.toString(quotes.getJobId()));
    }
}
