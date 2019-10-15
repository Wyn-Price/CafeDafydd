package com.wynprice.cafedafydd.common.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A util class containing the collectors used to collect streams.
 */
@Log4j2
public class UtilCollectors {

    /**
     * Converts a stream of characters to a string.
     * @return The collector to collect a stream of characters into a string.
     */
    public static Collector<Character, StringBuilder, String> toStringCollector() {
        return new CollectorImpl<>(
            StringBuilder::new,
            StringBuilder::append,
            StringBuilder::append,
            StringBuilder::toString
        );
    }

    public static <T, K, V> Collector<T, Map<K, V>, Map<K, V>> toHashMap(Function<T, K> keyGetter, Function<T, V> valueGetter) {
        return new CollectorImpl<>(
            HashMap::new,
            (map, t) -> map.put(keyGetter.apply(t), valueGetter.apply(t)),
            (map1, map2) -> { map1.putAll(map2); return map1; },
            map -> map
        );
    }

    /**
     * Converts the input into an optional. If there is more than one entry in this stream, an error is logged
     * and the item at index 0 is returned.
     * @param <T> The type
     * @return the collector to collect the stream into one optional.
     */
    public static <T> Collector<T, List<T>, Optional<T>> toSingleEntry() {
        return delegateStream(list -> {
            if(list.isEmpty()) {
                return Optional.empty();
            }
            if(list.size() == 1) {
                return Optional.of(list.get(0));
            }
            log.error("Expected 1 entry, found: " + list.size() + ". Stream=" + list.toString(), new IllegalArgumentException());
            return Optional.of(list.get(0));
        }
        );
    }

    /**
     * Used to delegate the streams. Allows collectors to be used like extension methods on the stream object.
     * @param func The function to use to return a new object
     * @param <T> the input type
     * @param <R> the output type
     * @return the collector that converts the stream from one object to another.
     */
    private static <T, R> Collector<T, List<T>, R> delegateStream(Function<List<T>, R> func)  {
        return new CollectorImpl<>(
            ArrayList::new,
            List::add,
            (list1, list2) -> {list1.addAll(list2); return list1; },
            func
        );
    }


    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    private static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
        private static final Set<Characteristics> CHARACTERISTICS = Collections.unmodifiableSet(new HashSet<>());
        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        @Override
        public Set<Characteristics> characteristics() {
            return CHARACTERISTICS;
        }
    }
}
