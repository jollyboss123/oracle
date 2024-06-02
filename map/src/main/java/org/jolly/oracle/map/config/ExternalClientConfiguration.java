package org.jolly.oracle.map.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class ExternalClientConfiguration {

    @Bean("polygonRestClient")
    public RestClient polygonRestClient(RestTemplateBuilder builder) {
        RestTemplate template = builder
//                .requestFactory(settings -> new BufferingClientHttpRequestFactory(
//                        ClientHttpRequestFactories.get(HttpComponentsClientHttpRequestFactory.class, settings)))
                .setConnectTimeout(Duration.ofSeconds(60))
                .setReadTimeout(Duration.ofSeconds(60))
                .build();
        return RestClient.builder(template).build();
    }
}
