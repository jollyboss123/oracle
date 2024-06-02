package org.jolly.oracle.map.polygon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.config.ApplicationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

/**
 * PolygonExternalClient is the class that interacts with the <a href="https://polygon.io/">Polygon.io</a> API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PolygonExternalClient {
    @Qualifier("polygonRestClient")
    private final RestClient restClient;
    @Value("${app.polygon.api-key}")
    private String apiKey;

    /**
     * Fetches aggregate data from the Polygon.io API based on the given request parameters.
     *
     * @param aggregatesRequest the request parameters for fetching aggregate data
     * @return the response containing aggregate data
     */
    public AggregatesResponse fetchAggregates(AggregatesRequest aggregatesRequest) {
        URI uri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("api.polygon.io")
                .path("/v2/aggs/ticker/{stocksTicker}/range/{multiplier}/{timespan}/{from}/{to}")
                .queryParamIfPresent("limit", Optional.ofNullable(aggregatesRequest.getLimit()))
                .buildAndExpand(aggregatesRequest.getTicker(), aggregatesRequest.getMultiplier(), aggregatesRequest.getTimespan().getValue(), aggregatesRequest.getFrom(), aggregatesRequest.getTo())
                .toUri();

        return restClient.get()
                .uri(uri)
                .headers(h -> {
                    h.add(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(apiKey));
                    h.add(HttpHeaders.CONTENT_TYPE, "application/json");
                })
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    log.error("client error code: {}, description: {}", response.getStatusCode(), response.getStatusText());
                    //TODO: throw custom exception
                })
                .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    log.error("server error code: {}, description: {}", response.getStatusCode(), response.getStatusText());
                    //TODO: throw custom exception
                }))
                .body(AggregatesResponse.class);
    }
}
