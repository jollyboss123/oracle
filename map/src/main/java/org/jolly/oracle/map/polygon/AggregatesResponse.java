package org.jolly.oracle.map.polygon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
@Jacksonized
public class AggregatesResponse {
    String ticker;
    boolean adjusted;
    String status;
    @Singular
    List<Result> results;

    @Value
    @Builder
    @Jacksonized
    public static class Result {
        @JsonProperty("c")
        BigDecimal closePrice;
    }
}
