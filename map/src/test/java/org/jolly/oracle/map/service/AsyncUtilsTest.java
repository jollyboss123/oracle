package org.jolly.oracle.map.service;

import org.jolly.oracle.map.async.AsyncUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class AsyncUtilsTest {

    @Test
    void anyOf() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<CompletableFuture<Integer>> cfs = Stream.of(10, 1, 5)
                .map(i -> CompletableFuture.supplyAsync(() -> returnWithDelay(i), executorService))
                .collect(Collectors.toList());

        Integer result = AsyncUtils.anyOf(cfs).join();
        assertThat(result).isEqualTo(1);
    }

    @Test
    void allOf() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<CompletableFuture<Integer>> cfs = Stream.of(3, 1, 2)
                .map(i -> CompletableFuture.supplyAsync(() -> returnWithDelay(i), executorService))
                .collect(Collectors.toList());

        List<Integer> result = AsyncUtils.allOf(cfs).join();
        assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void allOf_shortCircuiting() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<CompletableFuture<Integer>> cfs = Stream.of(10, 1, 5)
                .map(i -> CompletableFuture.supplyAsync(() -> {
                    if (i == 5) throw new IllegalStateException();
                    return returnWithDelay(i);
                }, executorService))
                .collect(Collectors.toList());

        CompletableFuture<List<Integer>> result = AsyncUtils.allOfOrException(cfs);

        await()
            .atMost(2, TimeUnit.SECONDS)
            .until(result::isCompletedExceptionally);
    }

    @Test
    void either() {
        CompletableFuture<Integer> f1 = CompletableFuture.completedFuture(42);
        CompletableFuture<Integer> f2 = CompletableFuture.failedFuture(new NullPointerException("oh no, anyway"));

        assertThat(AsyncUtils.either(f1, f2).join()).isEqualTo(42);
        assertThat(AsyncUtils.either(f2, f1).join()).isEqualTo(42);
    }

    @Test
    void either_exception() {
        CompletableFuture<Integer> f1 = CompletableFuture.failedFuture(new NullPointerException("oh no, anyway"));
        CompletableFuture<Integer> f2 = CompletableFuture.failedFuture(new NullPointerException("oh no, anyway"));

        assertThatThrownBy(() -> AsyncUtils.either(f1, f2).join()).hasCauseExactlyInstanceOf(NullPointerException.class);
    }

    private static int returnWithDelay(int i) {
        try {
            TimeUnit.SECONDS.sleep(i);
        } catch (InterruptedException e) {
            // ignoring consciously
        }

        return i;
    }
}
