package org.jolly.oracle.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.service.polygon.AggregatesRequest;
import org.jolly.oracle.map.service.polygon.PolygonExternalClient;
import org.jolly.oracle.map.service.yahoofinance.QuotesRequest;
import org.jolly.oracle.map.service.yahoofinance.YahooFinanceExternalClient;
import org.jolly.oracle.map.web.rest.VarRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapService {
    private static final String PRODUCER_QUOTES_NAME = "quotes-out-0";

    private final PolygonExternalClient polygonExternalClient;
    private final YahooFinanceExternalClient yahooFinanceExternalClient;
    private final StreamBridge streamBridge;
    @Qualifier("historicalDataTaskExecutor")
    private final Executor executor;

    /**
     * Fetches historical data for the provided assets from two different external clients.
     * The method pits the requests to both clients against each other, utilizing the result from the faster request.
     *
     * @param request the VarRequest containing the list of assets for which to fetch historical data
     * @return a {@code CompletableFuture<Void>}
     */
    public CompletableFuture<Void> execute(VarRequest request) {
        //TODO: add validation for whether valid stock/crypto before processing
        //TODO: can check db for existing records so don't have to set from 1 year
        //TODO: note polygon can only process 5 request per minute, would require some weightage handling or rate limit
        CompletableFuture<List<IQuoteResponse>> polygonResponsesFuture = AsyncUtils.inParallel(
                request.getAssets(),
                asset -> {
                    AggregatesRequest req = AggregatesRequest.builder()
                            .ticker(asset.getTicker())
                            .from(LocalDate.now().minusYears(1))
                            .to(LocalDate.now())
                            .timespan(AggregatesRequest.Timespan.DAY)
                            .build();

                    return polygonExternalClient.fetchAggregates(req);
                },
                executor
        );
        CompletableFuture<List<IQuoteResponse>> yfResponsesFuture = AsyncUtils.inParallel(
                request.getAssets(),
                asset -> {
                    QuotesRequest req = QuotesRequest.builder()
                            .ticker(asset.getTicker())
                            .from(LocalDate.now().minusYears(1))
                            .to(LocalDate.now())
                            .interval(QuotesRequest.Interval.DAILY)
                            .build();

                    return yahooFinanceExternalClient.fetchHistory(req);
                },
                executor
        );

        AsyncUtils.anyOf(polygonResponsesFuture, yfResponsesFuture)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        throw new MapServiceException("Fetch historical data error", ex);
                    }

                    if (result.isEmpty()) {
                        log.info("no historical data for the given assets");
                    } else {
                        QuotesMessage msg = QuotesMessage.from(result, request);
                        streamBridge.send(PRODUCER_QUOTES_NAME, msg);
                        log.info("{} published to kafka topic: {}", msg.getJobId(), PRODUCER_QUOTES_NAME);
                    }
                });

        return CompletableFuture.completedFuture(null);
    }
}
