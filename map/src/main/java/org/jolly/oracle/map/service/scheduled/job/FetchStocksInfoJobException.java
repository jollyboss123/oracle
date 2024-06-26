package org.jolly.oracle.map.service.scheduled.job;

import lombok.NoArgsConstructor;

import java.io.Serial;

@NoArgsConstructor
public class FetchStocksInfoJobException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4350937479836158211L;

    public FetchStocksInfoJobException(String message) {
        super(message);
    }

    public FetchStocksInfoJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public FetchStocksInfoJobException(Throwable cause) {
        super(cause);
    }
}
