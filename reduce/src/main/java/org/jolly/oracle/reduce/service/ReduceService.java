package org.jolly.oracle.reduce.service;

import lombok.extern.slf4j.Slf4j;
import org.ejml.simple.SimpleMatrix;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
public class ReduceService {
    private final Random random = new Random();
    private static final int SIMULATIONS = 10_000;
    private static final BigDecimal DAYS = BigDecimal.valueOf(20);
    private static final BigDecimal CONFIDENCE_INTERVAL = new BigDecimal("0.99");

    public void execute(QuotesMessage quotesMessage) {
        List<List<BigDecimal>> returns = returns(quotesMessage.getAssets());

        // single asset portfolio
        if (quotesMessage.getAssets().size() == 1) {
            BigDecimal valueAtRisk = valueAtRisk(new ArrayList<>(returns.getFirst()), CONFIDENCE_INTERVAL);
            log.info("expect to lose not more than: {} of the value of this position", valueAtRisk);
            return;
        }

        List<BigDecimal> weights = weights(quotesMessage.getPortfolioValue(), quotesMessage.getAssets());
        BigDecimal expectedReturns = expectedReturns(weights, returns);

        SimpleMatrix covMatrix = cov(toFloat(returns));
        BigDecimal stdDev = stdDeviation(weights, covMatrix);

        List<BigDecimal> scenarioReturn = new ArrayList<>();
        for (int i = 0; i < SIMULATIONS; i++) {
            BigDecimal zScore = randomZScore();
            scenarioReturn.add(scenarioGainLoss(quotesMessage.getPortfolioValue(), expectedReturns, stdDev, zScore, DAYS));
        }

        BigDecimal valueAtRisk = valueAtRisk(scenarioReturn, CONFIDENCE_INTERVAL);
        log.info("expect to lose not more than: {} of the value of this position", valueAtRisk);
    }

    protected static BigDecimal valueAtRisk(List<BigDecimal> scenarioReturn, BigDecimal confidenceInterval) {
        Collections.sort(scenarioReturn);
        BigDecimal oneMinusConfidence = BigDecimal.ONE.subtract(confidenceInterval.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        int idx = oneMinusConfidence.multiply(BigDecimal.valueOf(scenarioReturn.size())).setScale(0, RoundingMode.CEILING).intValue();

        // ensure the index is within bounds
        if (idx < 0) {
            idx = 0;
        }
        if (idx >= scenarioReturn.size()) {
            idx = scenarioReturn.size() - 1;
        }

        // return the negative value of the percentile to represent VaR
        return scenarioReturn.get(idx).negate();
    }

    protected static List<List<BigDecimal>> returns(Collection<QuotesMessage.Asset> assets) {
        return assets.stream()
                .map(QuotesMessage.Asset::getReturns)
                .toList();
    }

    protected static List<BigDecimal> weights(@Nullable BigDecimal portfolioValue, Collection<QuotesMessage.Asset> assets) {
        List<BigDecimal> weights;
        if (portfolioValue == null || portfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal totalValue = assets.stream()
                    .map(QuotesMessage.Asset::getValue)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalStateException("total portfolio value is 0");
            }
            weights = assets.stream()
                    .map(a -> a.getValue().divide(totalValue, 6, RoundingMode.HALF_UP))
                    .toList();
        } else {
            weights = assets.stream()
                    .map(a -> a.getValue().divide(portfolioValue, 6, RoundingMode.HALF_UP))
                    .toList();
        }

        assert weights.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(BigDecimal.ONE) == 0;
        return weights;
    }

    protected static BigDecimal expectedReturns(List<BigDecimal> weights, List<List<BigDecimal>> returns) {
        List<BigDecimal> returnsMeans = returns.stream()
                .map(ReduceService::mean)
                .toList();

        assert returnsMeans.size() == weights.size();
        BigDecimal expectedReturn = BigDecimal.ZERO;
        for (int i = 0; i < returnsMeans.size(); i++) {
            expectedReturn = expectedReturn.add(returnsMeans.get(i).multiply(weights.get(i)));
        }

        return expectedReturn;
    }

    protected static BigDecimal mean(Collection<BigDecimal> returns) {
        ReturnsMeanCollector res = returns.stream()
                .filter(Objects::nonNull)
                .collect(ReturnsMeanCollector.collector());

        return res.getSum().divide(res.getCount(), 6, RoundingMode.HALF_UP);
    }

    protected static float[][] toFloat(List<List<BigDecimal>> data) {
        return data.stream()
                .map(d -> d.stream()
                        .map(BigDecimal::floatValue)
                        .toList())
                .map(floatList -> {
                    float[] primitiveArray = new float[floatList.size()];
                    for (int i = 0; i < floatList.size(); i++) {
                        primitiveArray[i] = floatList.get(i);
                    }
                    return primitiveArray;
                })
                .toArray(float[][]::new);
    }

    protected static SimpleMatrix cov(float[][] data) {
        SimpleMatrix m = new SimpleMatrix(data);
        int observations = m.getNumRows();

        assert observations > 1;
        SimpleMatrix mTransposed = m.transpose();
        int variables = mTransposed.getNumRows();

        SimpleMatrix meansVector = new SimpleMatrix(variables, 1);
        for (int i = 0; i < variables; i++) {
            meansVector.set(i, 0, mTransposed.extractVector(true, i).elementSum() / observations);
        }

        SimpleMatrix covarianceMatrix = new SimpleMatrix(variables, variables);
        for (int i = 0; i < variables; i++) {
            for (int j = 0; j <= i; j++) {
                double covarianceValue = mTransposed.extractVector(true, i).minus(meansVector.get(i, 0))
                        .dot(mTransposed.extractVector(true, j).minus(meansVector.get(j, 0)).transpose()) / (observations - 1);
                covarianceMatrix.set(i, j, covarianceValue);
                covarianceMatrix.set(j, i, covarianceValue);
            }
        }

        return covarianceMatrix;
    }

    protected static BigDecimal stdDeviation(List<BigDecimal> weights, SimpleMatrix covMatrix) {
        if (weights.size() != covMatrix.getNumRows() || weights.size() != covMatrix.getNumCols()) {
            // Adjust the weights to match the covariance matrix dimensions
            weights = adjustWeights(weights, covMatrix.getNumRows());
        }

        SimpleMatrix weightsVector = new SimpleMatrix(weights.size(), 1);
        for (int i = 0; i < weights.size(); i++) {
            weightsVector.set(i, 0, weights.get(i).doubleValue());
        }

        // calculate variance: w' * Cov * w
        SimpleMatrix result = weightsVector.transpose().mult(covMatrix).mult(weightsVector);

        // result matrix is 1x1
        // extract the double value and calculate the square root for standard deviation
        double variance = result.get(0, 0);
        //TODO: handle NaN
        //TODO: handle Kafka when there is exceptions => backoff, retries etc.
        double stdDev = Math.sqrt(variance);

        // Convert the standard deviation back to BigDecimal and return
        return BigDecimal.valueOf(stdDev).setScale(6, RoundingMode.HALF_UP);
    }

    private static List<BigDecimal> adjustWeights(List<BigDecimal> weights, int size) {
        List<BigDecimal> adjustedWeights = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (i < weights.size()) {
                adjustedWeights.add(weights.get(i));
            } else {
                adjustedWeights.add(BigDecimal.ZERO);
            }
        }
        return adjustedWeights;
    }

    private static BigDecimal scenarioGainLoss(BigDecimal portfolioValue, BigDecimal portfolioExpectedReturn, BigDecimal portfolioStdDev, BigDecimal zScore, BigDecimal days) {
        BigDecimal gain = portfolioValue.multiply(portfolioExpectedReturn).multiply(days);
        BigDecimal loss = portfolioValue.multiply(portfolioStdDev).multiply(zScore).multiply(days.sqrt(new MathContext(10)));
        return gain.add(loss);
    }

    private BigDecimal randomZScore() {
        return BigDecimal.valueOf(random.nextGaussian(0, 1));
    }
}
