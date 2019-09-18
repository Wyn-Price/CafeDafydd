package com.wynprice.cafedafydd.server.utils;

import java.util.Comparator;
import java.util.List;

public class Algorithms {
    public static <T> void insert(List<T> list, T object, Comparator<? super T> comparator) {
        list.add(binarySearchIndex(list, object, comparator), object);
    }

    public static <T> int binarySearchIndex(List<T> list, T object, Comparator<? super T> comparator) {
        if(list.isEmpty()) {
            return 0;
        }
        int top = list.size() - 1;
        int bottom = 0;

        //Essentially a binary search to get the top+bottom range of where the object is.
        //If the object is found, it just inserts the object into that position of the list
        while(top - bottom > 1) {
            int mid = (top + bottom) / 2;

            int compared = comparator.compare(object, list.get(mid));
            if(compared == 0) {
                return mid;
            }
            if(comparator.compare(object, list.get(bottom)) == 0) {
                return bottom;
            }
            if(comparator.compare(object, list.get(top)) == 0) {
                return top;
            }

            if(compared < 0) {
                top = mid;
            } else {
                bottom = mid;
            }
        }

        //If the object should be inserted at index 0 then it won't be picked up in the above search algorithm
        if(bottom == 0 && comparator.compare(object, list.get(0)) < 0) {
            return 0;
        }

        //If the object should be inserted at the last index then it won't be picked up in the above search algorithm
        if(top == list.size() - 1 && comparator.compare(object, list.get(top)) > 0) {
            return top + 1;
        }

        return top;
    }

    public static <T> List<T> quickSort(List<T> sort, Comparator<? super T> comparator) {
        return doQuickSort(sort, 0, sort.size() - 1, comparator);
    }

    private static <T> List<T> doQuickSort(List<T> list, int bottom, int top, Comparator<? super T> comparator) {
        if (bottom < top) {
            T pivot = list.get(top);
            int base = (bottom-1);

            for (int iter = bottom; iter < top; iter++) {
                if (comparator.compare(list.get(iter), pivot) <= 0) {
                    base++;
                    swap(list, base, iter);
                }
            }
            swap(list, base+1, top);

            int pivotIndex = base+1;
            doQuickSort(list, bottom, pivotIndex-1, comparator);
            doQuickSort(list, pivotIndex+1, top, comparator);
        }
        return list;
    }

    private static <T> void swap(List<T> list, int from, int to) {
        T swapTemp = list.get(from);
        list.set(from, list.get(to));
        list.set(to, swapTemp);
    }


}
