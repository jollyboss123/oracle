package org.jolly.oracle.map.web.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
@Jacksonized
public class VarRequest {
    @Singular
    List<Asset> assets;

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
        BigDecimal value;
    }
}
