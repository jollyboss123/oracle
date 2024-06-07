package org.jolly.oracle.map.service;

import org.jetbrains.annotations.NotNull;
import org.jolly.oracle.map.service.yahoofinance.QuotesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IQuoteResponseTest {
    private static class IQuoteResponseImpl implements IQuoteResponse {

        @Override
        public String getTicker() {
            return "TEST";
        }

        @Override
        public List<IResult> getResults() {
            return List.of(
                    QuotesResponse.Result.builder()
                            .adjustedClose(new BigDecimal("124.216301"))
                            .build(),
                    QuotesResponse.Result.builder()
                            .adjustedClose(new BigDecimal("125.497498"))
                            .build(),
                    QuotesResponse.Result.builder()
                            .adjustedClose(new BigDecimal("124.166649"))
                            .build(),
                    QuotesResponse.Result.builder()
                            .adjustedClose(new BigDecimal("128.735245"))
                            .build(),
                    QuotesResponse.Result.builder()
                            .adjustedClose(new BigDecimal("129.261612"))
                            .build()
            );
        }

        @NotNull
        @Override
        public QuotesMessage.Asset toAsset(BigDecimal value) {
            return QuotesMessage.Asset.builder().build();
        }
    }

    private IQuoteResponse iQuoteResponse;

    @BeforeEach
    void setUp() {
        iQuoteResponse = new IQuoteResponseImpl();
    }

    @Test
    void testPctChange() {
        List<BigDecimal> expected = List.of(
                new BigDecimal("0.010314"),
                new BigDecimal("-0.010605"),
                new BigDecimal("0.036794"),
                new BigDecimal("0.004089")
        );

        assertEquals(expected, iQuoteResponse.toPctChanges());
    }
}
