package org.jolly.oracle.map.service;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
@Builder
@Jacksonized
@Slf4j
public class QuotesMessage {
    byte[] jobId;
    @Singular("quoteByTicker")
    Map<String, List<Quote>> quotesByTicker;
    //TODO: add weightage for each ticker and total portfolio value

    @Value
    @Builder
    @Jacksonized
    public static class Quote {
        String ticker;
        BigDecimal adjustedClose;
    }

    public static QuotesMessage from(List<IQuoteResponse> quoteResponses, @NonNull byte[] jobId) {
        if (ArrayUtils.isEmpty(jobId)) {
            throw new NullPointerException("jobId");
        }

        Map<String, List<Quote>> quotesByTicker = quoteResponses.stream()
                .flatMap(response -> response.toQuotes().stream())
                .collect(Collectors.groupingBy(Quote::getTicker));

        return QuotesMessage.builder()
                .jobId(jobId.clone())
                .quotesByTicker(quotesByTicker)
                .build();
    }
}
