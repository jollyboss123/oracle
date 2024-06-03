package org.jolly.oracle.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.service.polygon.AggregatesRequest;
import org.jolly.oracle.map.service.polygon.PolygonExternalClient;
import org.jolly.oracle.map.service.yahoofinance.QuotesRequest;
import org.jolly.oracle.map.service.yahoofinance.YahooFinanceExternalClient;
import org.jolly.oracle.map.web.rest.VarRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapService {
    private final PolygonExternalClient polygonExternalClient;
    private final YahooFinanceExternalClient yahooFinanceExternalClient;

    /**
     * Fetches historical data for the provided assets from two different external clients.
     * The method pits the requests to both clients against each other, utilizing the result from the faster request.
     *
     * @param request the VarRequest containing the list of assets for which to fetch historical data
     * @return a {@code CompletableFuture<Void>}
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> execute(VarRequest request) {
        // add validation for whether valid stock/crypto before processing
        // can check db for existing records so don't have to set from 1 year
        // note polygon can only process 5 request per minute, would require some weightage handling or rate limit
        List<CompletableFuture<IQuoteResponse>> aggregatesFuture = request.getAssets().stream()
                .map(asset -> CompletableFuture.supplyAsync(() -> {
                    AggregatesRequest req = AggregatesRequest.builder()
                            .ticker(asset.getTicker())
                            .from(LocalDate.now().minusYears(1))
                            .to(LocalDate.now())
                            .timespan(AggregatesRequest.Timespan.DAY)
                            .build();

                    return polygonExternalClient.fetchAggregates(req);
                        })
                )
                .toList();

        List<CompletableFuture<IQuoteResponse>> quotesFuture = request.getAssets().stream()
                .map(asset -> CompletableFuture.supplyAsync(() -> {
                    QuotesRequest req = QuotesRequest.builder()
                            .ticker(asset.getTicker())
                            .from(LocalDate.now().minusYears(1))
                            .to(LocalDate.now())
                            .interval(QuotesRequest.Interval.DAILY)
                            .build();

                    return yahooFinanceExternalClient.fetchHistory(req);
                }))
                .toList();

        CompletableFuture<List<IQuoteResponse>> aggregatesResponsesFuture = CompletableFuture.allOf(aggregatesFuture.toArray(new CompletableFuture[0]))
                .thenApply(v -> aggregatesFuture.stream()
                        .map(CompletableFuture::join)
                        .toList());
        CompletableFuture<List<IQuoteResponse>> quotesResponsesFuture = CompletableFuture.allOf(quotesFuture.toArray(new CompletableFuture[0]))
                .thenApply(v -> quotesFuture.stream()
                        .map(CompletableFuture::join)
                        .toList());

        CompletableFuture<Object> fastest = CompletableFuture.anyOf(aggregatesResponsesFuture, quotesResponsesFuture);
        fastest.whenComplete((result, ex) -> {
            if (ex != null) {
                throw new MapServiceException("Fetch historical data error", ex);
            }

            if (result instanceof List<?> list) {
                if (!list.isEmpty()) {
                    List<IQuoteResponse> responses = (List<IQuoteResponse>) list;
                    log.info(responses.toString());
                } else {
                    log.info("no historical data for the given assets");
                }
            }
        });

        return CompletableFuture.completedFuture(null);
    }
}
