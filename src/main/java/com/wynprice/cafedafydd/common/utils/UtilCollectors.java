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
import java.util.stream.Stream;

@Log4j2
public class UtilCollectors {

    public static Collector<Character, StringBuilder, String> toStringCollector() {
        return new CollectorImpl<>(
            StringBuilder::new,
            StringBuilder::append,
            StringBuilder::append,
            StringBuilder::toString
        );
    }

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

    public static Collector<String[], List<String[]>, Stream<Integer>> toFieldKey() {
        return delegateStream(list -> list.stream().map(array -> array[0]).filter(s -> s.matches("\\d+")).map(Integer::parseInt));
    }

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
