package com.wynprice.cafedafydd.server.utils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class AndList<T> implements Collection<T> {
    @Delegate
    private final List<T> backingList;


    public void and(Collection<T> elements, boolean inverted, boolean canSet) {
        if(this.isEmpty() && canSet) {
            this.addAll(elements);
        }
        this.removeIf(t -> inverted == elements.contains(t));
    }

}
