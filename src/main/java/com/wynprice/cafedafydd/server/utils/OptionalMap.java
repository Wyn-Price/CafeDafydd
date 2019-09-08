package com.wynprice.cafedafydd.server.utils;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@EqualsAndHashCode
public class OptionalMap<K, V> implements Map<K, V> {
    @Delegate private final Map<K, V> delegateMap;

    public Optional<V> getValue(K key) {
        return this.containsKey(key) ? Optional.ofNullable(this.get(key)) : Optional.empty();
    }

    public static <K, V> OptionalMap<K, V> emptyMap() {
        return new OptionalMap<K, V>(new HashMap<>()) {
            @Override
            public Optional<V> getValue(K key) {
                return Optional.empty();
            }
        };
    }
}
