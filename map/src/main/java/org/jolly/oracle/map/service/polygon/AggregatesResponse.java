package org.jolly.oracle.map.service.polygon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.jolly.oracle.map.service.IQuoteResponse;
import org.jolly.oracle.map.service.IResult;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        return new ArrayList<>(results);
    }

    @Value
    @Builder
    @Jacksonized
    public static class Result implements IResult {
        @JsonProperty("c")
        BigDecimal adjustedClose;
    }
}
