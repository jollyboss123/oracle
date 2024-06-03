package org.jolly.oracle.map.service;

import lombok.NoArgsConstructor;

import java.io.Serial;

@NoArgsConstructor
public class MapServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 6370223498813708446L;

    public MapServiceException(String message) {
        super(message);
    }

    public MapServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MapServiceException(Throwable cause) {
        super(cause);
    }
}
