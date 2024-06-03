package org.jolly.oracle.map.service.yahoofinance;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.jolly.oracle.map.service.IQuoteResponse;
import org.jolly.oracle.map.service.IResult;
import org.jolly.oracle.map.service.QuotesMessage;

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

    @Override
    public List<QuotesMessage.Quote> toQuotes() {
        return results.stream()
                .map(result -> QuotesMessage.Quote.builder()
                        .ticker(this.ticker)
                        .adjustedClose(result.getAdjustedClose())
                        .build())
                .toList();
    }
}
