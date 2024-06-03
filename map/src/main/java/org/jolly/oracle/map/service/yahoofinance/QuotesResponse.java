package org.jolly.oracle.map.service.yahoofinance;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.jolly.oracle.map.service.IQuoteResponse;
import org.jolly.oracle.map.service.IResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class QuotesResponse implements IQuoteResponse {
    String ticker;
    @Singular
    List<Result> results;

    @Override
    public List<IResult> getResults() {
        return new ArrayList<>(results);
    }

    @Value
    @Builder
    public static class Result implements IResult {
        BigDecimal adjustedClose;
    }
}
