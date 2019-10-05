package com.wynprice.cafedafydd.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayUtils {
    public static <T> List<T> asList(T... data) {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, data);
        return list;
    }
}
