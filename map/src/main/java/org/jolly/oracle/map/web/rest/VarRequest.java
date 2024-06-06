package org.jolly.oracle.map.web.rest;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.List;

@With
@Value
@Builder
@Jacksonized
@ValidPortfolioValue
public class VarRequest {
    @Singular
    List<Asset> assets;
    byte[] jobId;
    @Positive
    BigDecimal portfolioValue;

    @Value
    @Builder
    @Jacksonized
    public static class Asset {
        /**
         * name is the ticker symbol
         */
        @NotNull
        @NotEmpty
        @NotBlank
        String ticker;
        /**
         * value is the cash value held for this asset
         */
        @NotNull
        @Positive
        BigDecimal value;
    }
}
