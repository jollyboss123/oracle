package org.jolly.oracle.map.yahoofinance;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class QuotesResponse {
    String ticker;
    @Singular
    List<Result> results;

    @Value
    @Builder
    public static class Result {
        BigDecimal adjustedClose;
    }
}
