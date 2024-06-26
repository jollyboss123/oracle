package org.jolly.oracle.map.web.rest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jolly.oracle.map.web.rest.dto.VarRequest;

import java.math.BigDecimal;

public class PortfolioValueValidator implements ConstraintValidator<ValidPortfolioValue, VarRequest> {

    @Override
    public boolean isValid(VarRequest request, ConstraintValidatorContext constraintValidatorContext) {
        if (request.getPortfolioValue() == null) {
            return true;
        }

        BigDecimal totalAssetsValue = request.getAssets().stream()
                .map(VarRequest.Asset::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAssetsValue.equals(request.getPortfolioValue());
    }
}
