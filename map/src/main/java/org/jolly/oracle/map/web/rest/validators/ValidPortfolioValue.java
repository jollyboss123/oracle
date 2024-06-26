package org.jolly.oracle.map.web.rest.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = PortfolioValueValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidPortfolioValue {
    String message() default "portfolio value must match assets total value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
