package org.jolly.oracle.map.service.scheduled;

import lombok.NoArgsConstructor;

import java.io.Serial;

@NoArgsConstructor
public class AssetTickerServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 5850157355836223595L;

    public AssetTickerServiceException(String message) {
        super(message);
    }

    public AssetTickerServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssetTickerServiceException(Throwable cause) {
        super(cause);
    }
}
