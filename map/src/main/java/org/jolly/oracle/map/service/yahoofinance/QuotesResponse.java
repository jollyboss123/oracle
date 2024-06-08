package org.jolly.oracle.map.service.yahoofinance;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.jolly.oracle.map.service.IQuoteResponse;
import org.jolly.oracle.map.service.IResult;
import org.jolly.oracle.map.service.QuotesMessage;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class QuotesResponse implements IQuoteResponse {
    String ticker;
    @Singular
    List<Result> results;

    @Override
    public List<IResult> getResults() {
        return List.copyOf(results);
    }

    @Value
    @Builder
    public static class Result implements IResult {
        BigDecimal adjustedClose;
    }

    @NonNull
    @Override
    public QuotesMessage.Asset toAsset(BigDecimal value) {
        return QuotesMessage.Asset.builder()
                .ticker(ticker)
                .value(value)
                .returns(toPctChanges())
                .build();
    }
}
