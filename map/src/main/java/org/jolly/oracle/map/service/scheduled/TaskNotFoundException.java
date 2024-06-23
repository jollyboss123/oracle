package org.jolly.oracle.map.service.scheduled;

import lombok.NoArgsConstructor;

import java.io.Serial;

@NoArgsConstructor
public class TaskNotFoundException extends Exception {
    @Serial
    private static final long serialVersionUID = 4787397585133922503L;

    public TaskNotFoundException(String message) {
        super(message);
    }

    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskNotFoundException(Throwable cause) {
        super(cause);
    }
}
