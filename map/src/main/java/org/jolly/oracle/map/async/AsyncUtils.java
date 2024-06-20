package org.jolly.oracle.map.async;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AsyncUtils {

    public static <T> CompletableFuture<List<T>> allOf(CompletableFuture<T>... cfs) {
        return CompletableFuture.allOf(cfs)
                .thenApply(v -> Arrays.stream(cfs)
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    public static <T> CompletableFuture<List<T>> allOf(Collection<CompletableFuture<T>> cfs) {
        return CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0]))
                .thenApply(v -> cfs.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    public static <T> CompletableFuture<List<T>> allOfOrException(Collection<CompletableFuture<T>> cfs) {
        CompletableFuture<List<T>> result = allOf(cfs);

        for (CompletableFuture<?> cf : cfs) {
            cf.handle((res, ex) -> ex == null || result.completeExceptionally(ex));
        }

        return result;
    }

    public static <T> CompletableFuture<T> anyOf(Collection<CompletableFuture<T>> cfs) {
        return CompletableFuture.anyOf(cfs.toArray(new CompletableFuture[0]))
                .thenApply(o -> (T) o);
    }

    @SafeVarargs
    public static <T> CompletableFuture<T> anyOf(CompletableFuture<T>... cfs) {
        return CompletableFuture.anyOf(cfs)
                .thenApply(o -> (T) o);
    }

    public static <T> CompletableFuture<T> either(CompletableFuture<T> cf1, CompletableFuture<T> cf2) {
        CompletableFuture<T> result = new CompletableFuture<>();
        CompletableFuture.allOf(cf1, cf2).whenComplete((res, ex) -> {
            if (cf1.isCompletedExceptionally() && cf2.isCompletedExceptionally()) {
                result.completeExceptionally(ex);
            }
        });

        cf1.thenAccept(result::complete);
        cf2.thenAccept(result::complete);
        return result;
    }

    public static <T, R> CompletableFuture<List<R>> inParallel(Collection<? extends T> source, Function<T, R> mapper, Executor executor) {
        return source.stream()
                .map(i -> CompletableFuture.supplyAsync(() -> mapper.apply(i), executor))
                .collect(Collectors.collectingAndThen(Collectors.toList(), AsyncUtils::allOfOrException));
    }

    public static <T, R> CompletableFuture<List<R>> inParallelBatching(List<T> source, Function<T, R> mapper, Executor executor, int batches) {
        return BatchingSpliterator.partitioned(source, batches)
                .map(batch -> CompletableFuture.supplyAsync(() -> BatchingSpliterator.batching(mapper).apply(batch), executor))
                .collect(Collectors.collectingAndThen(Collectors.toList(), AsyncUtils::allOfOrException))
                .thenApply(list -> {
                    List<R> result = new ArrayList<>(source.size());
                    for (List<R> rs : list) {
                        result.addAll(rs);
                    }
                    return result;
                });
    }
}
