package org.jolly.oracle.reduce.service;

import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReduceServiceTest {

    @Test
    void expectedReturns() {
        QuotesMessage input = setupMockQuotesMessage();
        BigDecimal expected = ReduceService.expectedReturns(
                ReduceService.weights(input.getPortfolioValue(), List.copyOf(input.getAssets())),
                ReduceService.returns(List.copyOf(input.getAssets()))
        );
        BigDecimal expectedReturn = new BigDecimal("0.020000");

        assertEquals(expectedReturn.setScale(6, RoundingMode.HALF_UP), expected.setScale(6, RoundingMode.HALF_UP));
    }

    @Test
    void mean() {
        List<BigDecimal> returns = List.of(
                new BigDecimal("0.01"),
                new BigDecimal("0.02"),
                new BigDecimal("0.015"),
                new BigDecimal("0.03")
        );
        BigDecimal mean = ReduceService.mean(returns);
        BigDecimal expectedMean = new BigDecimal("0.018750");

        assertEquals(expectedMean, mean);
    }

    @Test
    void toFloat() {
        List<List<BigDecimal>> data = List.of(
                List.of(new BigDecimal("1.1"), new BigDecimal("2.2"), new BigDecimal("3.3")),
                List.of(new BigDecimal("4.4"), new BigDecimal("5.5"), new BigDecimal("6.6"))
        );

        float[][] expected = {
                {1.1f, 2.2f, 3.3f},
                {4.4f, 5.5f, 6.6f}
        };

        float[][] result = ReduceService.toFloat(data);

        assertTrue(Arrays.deepEquals(expected, result));
    }

    @Test
    void cov() {
        float[][] data = {
                {4.0f, 2.0f, 0.60f},
                {4.2f, 2.1f, 0.59f},
                {3.9f, 2.0f, 0.58f},
                {4.3f, 2.1f, 0.62f},
                {4.1f, 2.2f, 0.63f}
        };

        SimpleMatrix expected = new SimpleMatrix(new double[][]{
                {0.025, 0.0075, 0.00175},
                {0.0075, 0.007, 0.00135},
                {0.00175, 0.00135, 0.00043}
        });

        SimpleMatrix result = ReduceService.cov(data);

        assertTrue(expected.isIdentical(result, 0.0001), () -> "expected: %s, gotten: %s".formatted(expected, result));
    }

    @Test
    void stdDeviation() {
        List<BigDecimal> weights = Arrays.asList(
                new BigDecimal("0.5"),
                new BigDecimal("0.3"),
                new BigDecimal("0.2")
        );

        SimpleMatrix covMatrix = new SimpleMatrix(new double[][]{
                {0.04, 0.006, 0.005},
                {0.006, 0.09, 0.002},
                {0.005, 0.002, 0.01}
        });

        BigDecimal expected = new BigDecimal("0.146765");

        BigDecimal actual = ReduceService.stdDeviation(weights, covMatrix);

        assertEquals(expected, actual, "expected: %s, gotten: %s".formatted(expected, actual));
    }

    @Test
    void stdDeviation_singleWeight() {
        List<BigDecimal> weights = List.of(new BigDecimal("1"));

        SimpleMatrix covMatrix = new SimpleMatrix(new double[][]{
                {0.04, 0.006, 0.005},
                {0.006, 0.09, 0.002},
                {0.005, 0.002, 0.01}
        });

        BigDecimal expected = new BigDecimal("0.200000");

        BigDecimal actual = ReduceService.stdDeviation(weights, covMatrix);

        assertEquals(expected, actual);
    }

    @Test
    void stdDeviation_mismatchWeightAndCovMatrixSize() {
        List<BigDecimal> weights = Arrays.asList(
                new BigDecimal("0.5"),
                new BigDecimal("0.5")
        );

        SimpleMatrix covMatrix = new SimpleMatrix(new double[][]{
                {0.04, 0.006, 0.005},
                {0.006, 0.09, 0.002},
                {0.005, 0.002, 0.01}
        });

        BigDecimal expected = new BigDecimal("0.188414").setScale(6, RoundingMode.HALF_UP);

        BigDecimal actual = ReduceService.stdDeviation(weights, covMatrix);

        assertEquals(expected, actual, "expected: %s, gotten: %s".formatted(expected, actual));
    }

    @Test
    void valueAtRisk() {
        List<BigDecimal> scenarioReturn = Arrays.asList(
                new BigDecimal("-0.02"),
                new BigDecimal("0.01"),
                new BigDecimal("-0.03"),
                new BigDecimal("0.04"),
                new BigDecimal("0.01"),
                new BigDecimal("-0.01"),
                new BigDecimal("0.02"),
                new BigDecimal("-0.02")
        );

        BigDecimal confidenceInterval = new BigDecimal("95");

        // Manually calculating the expected value at risk at 95% confidence level
        // Sorted returns: [-0.03, -0.02, -0.02, -0.01, 0.01, 0.01, 0.02, 0.04]
        // 5% percentile value (idx 0.05 * 8 = 0.4 -> idx 1 (0-based index) after sorting -> -0.02)
        BigDecimal expected = new BigDecimal("0.02");

        BigDecimal actual = ReduceService.valueAtRisk(scenarioReturn, confidenceInterval);

        assertEquals(expected, actual, () -> "expected: %s, gotten: %s".formatted(expected, actual));
    }

    private static QuotesMessage setupMockQuotesMessage() {
        return QuotesMessage.builder()
                .jobId(new byte[]{1, 2, 3})
                .portfolioValue(new BigDecimal("1000"))
                .asset(QuotesMessage.Asset.builder()
                        .value(new BigDecimal("500"))
                        .ticker("AAPL")
                        .returns(List.of(
                                new BigDecimal("0.01"),
                                new BigDecimal("0.02"),
                                new BigDecimal("0.015")
                        ))
                        .build())
                .asset(QuotesMessage.Asset.builder()
                        .value(new BigDecimal("500"))
                        .ticker("MSFT")
                        .returns(List.of(
                                new BigDecimal("0.03"),
                                new BigDecimal("0.025"),
                                new BigDecimal("0.02")
                        ))
                        .build())
                .build();
    }
}
