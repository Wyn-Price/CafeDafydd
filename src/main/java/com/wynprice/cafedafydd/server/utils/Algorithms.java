package com.wynprice.cafedafydd.server.utils;

import com.wynprice.cafedafydd.common.utils.DatabaseRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * The class holding all the algorithms used by the databases.
 */
public class Algorithms {

    /**
     * Inserts the object into the list at the correct position.
     * @param list the list to insert into. MUST BE SORTED
     * @param object the object to insert
     * @param comparator the comparator for the list/object
     * @param <T> the type of the list
     * @see #binarySearchIndex(List, Function, Object, Comparator, boolean)
     */
    public static <T> void insert(List<T> list, T object, Comparator<? super T> comparator) {
        list.add(binarySearchIndex(list, t -> t, object, comparator, false), object);
    }

    /**
     * Does a mapped search for elements in the list.
     * @param list the list to search in
     * @param mapper the mapper to map one type to the other
     * @param object the object to search for
     * @param comparator the comparator to compare
     * @param <R> the original list type
     * @param <T> the type to search with, taken from {@code mapper}
     * @return the found list object in that list, or {@link Optional#empty()} if none could be found.
     */
    public static <R, T> Optional<R> doMappedSearch(List<R> list, Function<R, T> mapper, T object, Comparator<? super T> comparator) {
        int index = binarySearchIndex(list, mapper, object, comparator,true);
        return index < 0 ? Optional.empty() : Optional.of(list.get(index));
    }

    /**
     * Searches a list with the binary search to return the sublist of these elements inside the main list.
     * Essentially, searches the list for the entry, then gets the whole list around that point that compares the same.
     * <pre>{@code
     * array = [0, 1, 1, 2, 2, 2, 3]
     *
     * splicedBinarySearch(array, 1) -> [1, 1]
     * splicedBinarySearch(array, 2) -> [2, 2, 2]
     * splicedBinarySearch(array, 3) -> [3]
     * splicedBinarySearch(array, 4) -> []
     * }</pre>
     * @param list the main list to search within.
     * @param mapper the mapper to map the object from {@code E} to {@code T}
     * @param object the object to search for
     * @param comparator the comparator to compare the objects
     * @param <E> the list type
     * @param <T> the type to search for. Taken from the list then mapped with the {@code mapper}
     * @return the sub list within the main list, or an empty list if none could be found.
     * @see #binarySearchIndex(List, Function, Object, Comparator, boolean)
     */
    public static <E, T> List<E> splicedBinarySearch(List<E> list, Function<E, T> mapper, T object, Comparator<? super T> comparator) {
        //Get the searched index of the object. If it's less than 0, then the element doesn't exist in the list and an empty array should be returned.
        int index = binarySearchIndex(list, mapper, object, comparator, true);
        if(index < 0) {
            return new ArrayList<>();
        }

        //The min represents the minimum index where the element compares the same.
        int min = index;
        //While the elements at the position `min` compare to the `object`, lower min
        do {
            min--;
        } while (min >= 0 && comparator.compare(object, mapper.apply(list.get(min))) == 0);

        //The max represents the maximum index where the element compares the same.
        int max = index;
        //While the elements at the position `max` compare to the `object`, increase max
        do {
            max++;
        } while (max < list.size() && comparator.compare(object, mapper.apply(list.get(max))) == 0);

        //return the sublist from min -> max
        //The +1 comes into play as both min and max are going to be 1 off where the last element is
        //but as subList is (inclusive, exclusive), max doesn't need to change
        return list.subList(min+1, max);
    }

    /**
     * Gets the index where the specified object exists in the list
     * @param list the list to look in
     * @param mapper the mapper to map the object from {@code E} to {@code T}
     * @param object the object to search for
     * @param comparator the comparator to compare the objects
     * @param mustExist if true, then if the element doesn't exist the returned index will be -1
     *                  If false then if the element doesn't exist the returned index will be where the element would exist.
     * @param <E> the list type
     * @param <T> the type to search for. Taken from the list then mapped with the {@code mapper}
     * @return the index of where the specified object is, or if it doesn't exist and {@code mustExist} is false where it would be, or if true then -1.
     */
    private static <E, T> int binarySearchIndex(List<E> list, Function<E, T> mapper, T object, Comparator<? super T> comparator, boolean mustExist) {
        //Don't bother searching
        if(list.isEmpty()) {
            return mustExist ? -1 : 0;
        }

        //Set the top and bottom bounds
        int top = list.size() - 1;
        int bottom = 0;


        //If the element is at the top or the bottom of the list, then it won't get picked by the rest of the algorithm, so just do a check here.
        if(comparator.compare(object, mapper.apply(list.get(bottom))) == 0) {
            return bottom;
        }
        if(comparator.compare(object, mapper.apply(list.get(top))) == 0) {
            return top;
        }

        //If the element doesn't have to exist, and it's smaller than the first element, or larger than the last element then return 0 or the top + 1
        if(!mustExist) {
            //Element at 0 is smaller than the first element
            if(comparator.compare(object, mapper.apply(list.get(0))) < 0) {
                return 0;
            }

            //Last element in the list is smaller than the specified object
            if(top == list.size() - 1 && comparator.compare(object, mapper.apply(list.get(top))) > 0) {
                return top + 1;
            }
        }

        //While the bounds are not nextChar to each-other get the middle of the bounds and set that as the midpoint.
        while(top - bottom > 1) {
            int mid = (top + bottom) / 2;

            //If the middle point is comparably the same as the given object, return the midpoint
            int compared = comparator.compare(object, mapper.apply(list.get(mid)));
            if(compared == 0) {
                return mid;
            }

            //If the object is less than the element at `mid`, set the top boundary to mid. Otherwise set the bottom boundary to mid
            if(compared < 0) {
                top = mid;
            } else {
                bottom = mid;
            }
        }

        //If the code reaches here then the element could not be found, and top and mid are nextChar to eachother.

        //If the element must exist, and can't be guessed return -1, as it doesn't exist
        if(mustExist) {
            return -1;
        }

        //Return the position where the element would be
        return top;
    }

    /**
     * Sorts the whole list
     * @param sort the list to sort
     * @param comparator the comparator for the list
     * @param <T> the list type
     * @return the same object as {@code sort}
     */
    public static <T> List<T> quickSort(List<T> sort, Comparator<? super T> comparator) {
        return doQuickSort(sort, 0, sort.size() - 1, comparator);
    }

    /**
     * Performs a quicksort on the list
     * @param list the list to sort
     * @param bottom the bottom boundary
     * @param top the top boundary
     * @param comparator the comparator to
     * @param <T> the list type
     * @return the same object as {@code sort}
     */
    private static <T> List<T> doQuickSort(List<T> list, int bottom, int top, Comparator<? super T> comparator) {
        //If the bottom boundary is below the top boundry do the sort, otherwise we have a 0 sized section to sort, which means it's already sorted.
        if (bottom < top) {

            //get the pivot point. In this algorithm we just get the top element.
            T pivot = list.get(top);

            //the point where the swapped elements should be inserted.
            int insertBase = bottom-1;

            //Go from the bottom to the top, comparing the elements and if they are less than the pivot, increment the insertBase and swap the elements at the insertBase
            for (int iter = bottom; iter < top; iter++) {
                if (comparator.compare(list.get(iter), pivot) <= 0) {
                    swap(list, ++insertBase, iter);
                }
            }
            //Swap the elements after the insert base and the top
            swap(list, insertBase+1, top);


            //get the pivot index, and sort the element from: bottom -> pivotIndex-1 and pivotIndex+1 -> top.
            int pivotIndex = insertBase+1;
            doQuickSort(list, bottom, pivotIndex-1, comparator);
            doQuickSort(list, pivotIndex+1, top, comparator);
        }
        return list;
    }

    /**
     * Swap the elements in the list. Swaps around the elements at {@code from} and {@code to}
     * @param list the list to swap the types
     * @param from the index of an element
     * @param to the index of another element
     * @param <T> the type
     */
    private static <T> void swap(List<T> list, int from, int to) {
        T swapTemp = list.get(from);
        list.set(from, list.get(to));
        list.set(to, swapTemp);
    }


}
