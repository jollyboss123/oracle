package org.jolly.oracle.map.web.rest.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = TickerValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidTickers {
    String message() default "ticker(s) does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
