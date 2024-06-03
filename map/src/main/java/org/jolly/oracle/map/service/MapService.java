package org.jolly.oracle.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.polygon.PolygonExternalClient;
import org.jolly.oracle.map.web.rest.VarRequest;
import org.jolly.oracle.map.yahoofinance.QuotesRequest;
import org.jolly.oracle.map.yahoofinance.YahooFinanceExternalClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapService {
    private final PolygonExternalClient polygonExternalClient;
    private final YahooFinanceExternalClient yahooFinanceExternalClient;

    @Async("taskExecutor")
    public CompletableFuture<Void> execute(VarRequest request) {
        // can check db for existing records so don't have to set from to 1 year
//        List<CompletableFuture<AggregatesResponse>> aggregatesFuture = request.getAssets().stream()
//                .map(asset -> CompletableFuture.supplyAsync(() -> {
//                    AggregatesRequest req = AggregatesRequest.builder()
//                            .ticker(asset.getTicker())
//                            .from(LocalDate.now().minusYears(1))
//                            .to(LocalDate.now())
//                            .timespan(AggregatesRequest.Timespan.DAY)
//                            .build();
//
//                    return polygonExternalClient.fetchAggregates(req);
//                        })
//                )
//                .toList();
//
//        List<AggregatesResponse> aggregatesResponses = aggregatesFuture.stream()
//                .map(CompletableFuture::join)
//                .toList();
//
//        log.info(aggregatesResponses.toString());

        QuotesRequest req = QuotesRequest.builder()
                .ticker("AAPL")
                .from(LocalDate.now().minusYears(1))
                .to(LocalDate.now())
                .interval(QuotesRequest.Interval.DAILY)
                .build();
        log.info(yahooFinanceExternalClient.fetchHistory(req).toString());

        return CompletableFuture.completedFuture(null);
    }
}
