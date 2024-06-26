package org.jolly.oracle.map.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class BatchingSpliterator<T> implements Spliterator<List<T>> {

    private final List<T> source;
    private final int maxChunks;

    private int chunks;
    private int chunkSize;
    private int consumed;

    private BatchingSpliterator(List<T> list, int batches) {
        if (batches < 1) {
            throw new IllegalArgumentException("batches cannot be lower than one");
        }

        this.source = list;
        this.chunks = batches;
        this.maxChunks = Math.min(list.size(), batches);
        this.chunkSize = (int) Math.ceil(((double) source.size()) / batches);
    }

    static <T> Stream<List<T>> partitioned(List<T> list, int numberOfParts) {
        int size = list.size();

        if (size <= numberOfParts) {
            return asSingletonListStream(list);
        } else if (size == 0) {
            return Stream.empty();
        } else if (numberOfParts == 1) {
            return Stream.of(list);
        } else {
            return StreamSupport.stream(new BatchingSpliterator<>(list, numberOfParts), false);
        }
    }

    private static <T> Stream<List<T>> asSingletonListStream(List<T> list) {
        Stream.Builder<List<T>> acc = Stream.builder();
        for (T t : list) {
            acc.add(List.of(t));
        }
        return acc.build();
    }

    static <T, R> Function<List<T>, List<R>> batching(Function<T, R> mapper) {
        return batch -> {
            List<R> list = new ArrayList<>(batch.size());
            for (T t : batch) {
                list.add(mapper.apply(t));
            }
            return list;
        };
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<T>> action) {
        if (consumed < source.size() && chunks != 0) {
            List<T> batch = source.subList(consumed, consumed + chunkSize);
            consumed += chunkSize;
            chunkSize = (int) Math.ceil(((double) (source.size() - consumed)) / --chunks);
            action.accept(batch);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Spliterator<List<T>> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return maxChunks;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.SIZED;
    }
}
