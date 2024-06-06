package org.jolly.oracle.map.web.rest.errors;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

import java.io.Serial;
import java.net.URI;
import java.util.Map;

@Getter
public class BadRequestAlertException extends ErrorResponseException {
    @Serial
    private static final long serialVersionUID = 6921886952533745529L;

    private final String entityName;
    private final String errorKey;

    public BadRequestAlertException(String defaultMessage, String entityName, String errorKey) {
        this(ErrorConstants.DEFAULT_TYPE, defaultMessage, entityName, errorKey);
    }

    public BadRequestAlertException(URI type, String defaultMessage, String entityName, String errorKey) {
        super(
            HttpStatus.BAD_REQUEST,
            ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                    .withStatus(HttpStatus.BAD_REQUEST.value())
                    .withType(type)
                    .withTitle(defaultMessage)
                    .withProperties(Map.of(
                            "message", "error." + errorKey,
                            "params", entityName
                    ))
                    .build()
                    .asProblemDetail(),
            null
        );
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    public ProblemDetailWithCause getProblemDetailWithCause() {
        return ProblemDetailWithCause.from(this.getBody());
    }
}
