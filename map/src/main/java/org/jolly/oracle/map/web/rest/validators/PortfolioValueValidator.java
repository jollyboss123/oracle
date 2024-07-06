package org.jolly.oracle.map.web.rest.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.jolly.oracle.map.service.ValidationService;
import org.jolly.oracle.map.web.rest.dto.VarRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PortfolioValueValidator implements ConstraintValidator<ValidPortfolioValue, VarRequest> {
    private final ValidationService validationService;

    @Override
    public boolean isValid(VarRequest request, ConstraintValidatorContext constraintValidatorContext) {
        return validationService.validatePortfolioValue(request);
    }
}
