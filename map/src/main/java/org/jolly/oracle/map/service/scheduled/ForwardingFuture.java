package org.jolly.oracle.map.service.scheduled;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class ForwardingFuture<V> extends ForwardingObject implements Future<V> {
    protected ForwardingFuture() {}

    @Override
    protected abstract Future<? extends V> delegate();

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate().cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return delegate().isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate().isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return delegate().get();
    }

    @Override
    public V get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate().get(timeout, unit);
    }

    public abstract static class SimpleForwardingFuture<V> extends ForwardingFuture<V> {
        private final Future<V> delegate;

        protected SimpleForwardingFuture(Future<V> delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        protected Future<? extends V> delegate() {
            return delegate;
        }
    }
}
