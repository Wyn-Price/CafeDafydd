package com.wynprice.cafedafydd.server.utils;

import com.wynprice.cafedafydd.common.utils.DatabaseRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Algorithms {
    public static <T> void insert(List<T> list, T object, Comparator<? super T> comparator) {
        list.add(binarySearchIndex(list, t -> t, object, comparator, false), object);
    }

    public static <T> Optional<T> doBinarySearch(List<T> list, T object, Comparator<? super T> comparator) {
        int index = binarySearchIndex(list, t -> t, object, comparator, true);
        return index < 0 ? Optional.empty() : Optional.of(list.get(index));
    }

    public static <T> Optional<DatabaseRecord> doRecordSearch(List<DatabaseRecord> list, Function<DatabaseRecord, T> mapper, T object, Comparator<? super T> comparator) {
        int index = binarySearchIndex(list, mapper, object, comparator,true);
        return index < 0 ? Optional.empty() : Optional.of(list.get(index));
    }

    //Searches the list for the entry, then gets the whole list around that point that compares the same.
    //array = [0, 1, 1, 2, 2, 2, 3]
    //splicedBinarySearch(array, 1) -> [1, 1]
    //splicedBinarySearch(array, 2) -> [2, 2, 2]
    public static <E, T> List<E> splicedBinarySearch(List<E> list, Function<E, T> mapper, T object, Comparator<? super T> comparator) {
        int index = binarySearchIndex(list, mapper, object, comparator, true);
        if(index < 0) {
            return new ArrayList<>();
        }

        int min = index;
        int max = index;

        do {
            min--;
        } while (min >= 0 && comparator.compare(object, mapper.apply(list.get(min))) == 0);

        do {
            max++;
        } while (max < list.size() && comparator.compare(object, mapper.apply(list.get(max))) == 0);



        return list.subList(min+1, max);
    }

    private static <E, T> int binarySearchIndex(List<E> list, Function<E, T> mapper, T object, Comparator<? super T> comparator, boolean mustExist) {
        if(list.isEmpty()) {
            return 0;
        }
        int top = list.size() - 1;
        int bottom = 0;

        //Essentially a binary search to get the top+bottom range of where the object is.
        //If the object is found, it just inserts the object into that position of the list
        while(top - bottom > 1) {
            int mid = (top + bottom) / 2;

            int compared = comparator.compare(object, mapper.apply(list.get(mid)));
            if(compared == 0) {
                return mid;
            }
            if(comparator.compare(object, mapper.apply(list.get(bottom))) == 0) {
                return bottom;
            }
            if(comparator.compare(object, mapper.apply(list.get(top))) == 0) {
                return top;
            }

            if(compared < 0) {
                top = mid;
            } else {
                bottom = mid;
            }
        }

        if(mustExist) {
            return -1;
        }

        //If the object should be inserted at index 0 then it won't be picked up in the above search algorithm
        if(bottom == 0 && comparator.compare(object, mapper.apply(list.get(0))) < 0) {
            return 0;
        }

        //If the object should be inserted at the last index then it won't be picked up in the above search algorithm
        if(top == list.size() - 1 && comparator.compare(object, mapper.apply(list.get(top))) > 0) {
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
