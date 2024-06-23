package org.jolly.oracle.map.service.scheduled;

import java.util.concurrent.Future;

public class CallbackFuture<V> extends ForwardingFuture.SimpleForwardingFuture<V> {
    private final Runnable cancelCallback;

    public CallbackFuture(Future<V> delegate, Runnable cancelCallback) {
        super(delegate);
        this.cancelCallback = cancelCallback;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = super.cancel(mayInterruptIfRunning);
        if (cancelled) {
            cancelCallback.run();
        }
        return cancelled;
    }
}
