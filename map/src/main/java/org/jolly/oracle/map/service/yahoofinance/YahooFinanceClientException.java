package org.jolly.oracle.map.service.yahoofinance;

import lombok.NoArgsConstructor;

import java.io.Serial;

@NoArgsConstructor
public class YahooFinanceClientException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -579157284572184652L;

    public YahooFinanceClientException(String message) {
        super(message);
    }

    public YahooFinanceClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public YahooFinanceClientException(Throwable cause) {
        super(cause);
    }
}
