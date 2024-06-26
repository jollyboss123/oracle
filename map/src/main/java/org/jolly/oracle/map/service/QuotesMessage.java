package org.jolly.oracle.map.service;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jolly.oracle.map.web.rest.dto.VarRequest;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.HashMap;
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

    public static QuotesMessage from(List<IQuoteResponse> quoteResponses, @NonNull VarRequest varRequest) {
        if (ArrayUtils.isEmpty(varRequest.getJobId())) {
            throw new NullPointerException("jobId");
        }

        Map<String, BigDecimal> tickerValueMap = varRequest.getAssets().stream()
                        .collect(
                                Collectors.toMap(
                                    VarRequest.Asset::getTicker,
                                    VarRequest.Asset::getValue,
                                    (o, n) -> o,
                                    HashMap::new
                        ));

        Set<Asset> assets = quoteResponses.stream()
                .map(response -> {
                    if (!tickerValueMap.containsKey(response.getTicker())) {
                        log.error("{} does not exist in var request", response.getTicker());
                        throw new IllegalStateException("Ticker does not exist in request");
                    }

                    return response.toAsset(tickerValueMap.get(response.getTicker()));
                })
                .collect(Collectors.toSet());

        return QuotesMessage.builder()
                .jobId(varRequest.getJobId().clone())
                .portfolioValue(varRequest.getPortfolioValue())
                .assets(assets)
                .build();
    }
}
