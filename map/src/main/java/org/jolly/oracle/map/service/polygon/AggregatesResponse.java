package org.jolly.oracle.map.service.polygon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.jetbrains.annotations.NotNull;
import org.jolly.oracle.map.service.IQuoteResponse;
import org.jolly.oracle.map.service.IResult;
import org.jolly.oracle.map.service.QuotesMessage;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
@Jacksonized
public class AggregatesResponse implements IQuoteResponse {
    String ticker;
    boolean adjusted;
    String status;
    @Singular
    List<Result> results;

    @Override
    public List<IResult> getResults() {
        return List.copyOf(this.results);
    }

    @Value
    @Builder
    @Jacksonized
    public static class Result implements IResult {
        @JsonProperty("c")
        BigDecimal adjustedClose;
    }

    @NotNull
    @Override
    public QuotesMessage.Asset toAsset(BigDecimal value) {
        return QuotesMessage.Asset.builder()
                .ticker(ticker)
                .value(value)
                .returns(toPctChanges())
                .build();
    }
}
