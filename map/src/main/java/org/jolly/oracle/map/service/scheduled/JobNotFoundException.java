package org.jolly.oracle.map.service.scheduled;

import lombok.NoArgsConstructor;

import java.io.Serial;

@NoArgsConstructor
public class JobNotFoundException extends Exception {
    @Serial
    private static final long serialVersionUID = 4787397585133922503L;

    public JobNotFoundException(String message) {
        super(message);
    }

    public JobNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobNotFoundException(Throwable cause) {
        super(cause);
    }
}
