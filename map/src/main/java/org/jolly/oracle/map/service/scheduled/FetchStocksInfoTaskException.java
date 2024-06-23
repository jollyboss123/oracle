package org.jolly.oracle.map.service.scheduled;

import lombok.NoArgsConstructor;

import java.io.Serial;

@NoArgsConstructor
public class FetchStocksInfoTaskException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4350937479836158211L;

    public FetchStocksInfoTaskException(String message) {
        super(message);
    }

    public FetchStocksInfoTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public FetchStocksInfoTaskException(Throwable cause) {
        super(cause);
    }
}
