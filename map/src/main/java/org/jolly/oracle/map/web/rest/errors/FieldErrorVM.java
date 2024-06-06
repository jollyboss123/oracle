package org.jolly.oracle.map.web.rest.errors;

import lombok.Builder;
import lombok.Value;

import java.io.Serial;
import java.io.Serializable;

@Value
@Builder
public class FieldErrorVM implements Serializable {
    @Serial
    private static final long serialVersionUID = -1277403676504587551L;

    String objectName;
    String field;
    String message;
}
