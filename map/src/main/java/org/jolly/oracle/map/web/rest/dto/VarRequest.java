package org.jolly.oracle.map.web.rest.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import org.jolly.oracle.map.web.rest.validators.ValidPortfolioValue;
import org.jolly.oracle.map.web.rest.validators.ValidTickers;

import java.math.BigDecimal;
import java.util.Set;

@With
@Value
@Builder
@Jacksonized
@ValidPortfolioValue
@ValidTickers
public class VarRequest {
    @Singular
    Set<Asset> assets;
    byte[] jobId;
    @Positive
    BigDecimal portfolioValue;
    @Positive
    int holdingPeriod; // in days
    @Positive
    @Max(100)
    int confidenceLevel; // as a percentage

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
