package org.jolly.oracle.map.service.polygon;

import lombok.NoArgsConstructor;

import java.io.Serial;

@NoArgsConstructor
public class PolygonClientException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -617537859292683466L;

    public PolygonClientException(String message) {
        super(message);
    }

    public PolygonClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolygonClientException(Throwable cause) {
        super(cause);
    }
}
