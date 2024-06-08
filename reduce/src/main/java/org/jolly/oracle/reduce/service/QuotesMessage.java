package org.jolly.oracle.reduce.service;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@Builder
@Jacksonized
@Slf4j
public class QuotesMessage {
    byte[] jobId;
    BigDecimal portfolioValue;
    @Singular
    Set<Asset> assets;

    @Value
    @Builder
    @Jacksonized
    public static class Asset {
        BigDecimal value;
        String ticker;
        @Singular
        List<BigDecimal> returns;
    }
}
