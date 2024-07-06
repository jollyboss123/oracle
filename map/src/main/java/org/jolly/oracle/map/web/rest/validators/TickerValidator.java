package org.jolly.oracle.map.web.rest.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.service.ValidationService;
import org.jolly.oracle.map.web.rest.dto.VarRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TickerValidator implements ConstraintValidator<ValidTickers, VarRequest> {
    private final ValidationService validationService;

    @Override
    public boolean isValid(VarRequest request, ConstraintValidatorContext context) {
        //TODO: maybe bloom filter here
        List<String> invalidTickers = validationService.validateTickers(new ArrayList<>(request.getAssets()));
        if (!invalidTickers.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("tickers " + invalidTickers + " does not exists")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
