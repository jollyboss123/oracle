package org.jolly.oracle.map.service;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public interface IQuoteResponse {
    String getTicker();
    List<IResult> getResults();
    @NonNull QuotesMessage.Asset toAsset(BigDecimal value);

    /**
     * Calculates the percentage changes based on adjusted close prices.
     * Skips calculations when the base or current value is zero to avoid division by zero.
     * Uses {@link RoundingMode#HALF_UP} for percentage calculations.
     *
     * @return List of values representing the percentage changes.
     */
    default List<BigDecimal> toPctChanges() {
        List<BigDecimal> pctChanges = new ArrayList<>();
        BigDecimal base = null;

        for (IResult result : getResults()) {
            BigDecimal curr = result.getAdjustedClose();

            if (base != null && base.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal change = curr.subtract(base)
                        .divide(base, 6, RoundingMode.HALF_UP);
                pctChanges.add(change);
            }

            base = curr;
        }

        return pctChanges;
    }
}
