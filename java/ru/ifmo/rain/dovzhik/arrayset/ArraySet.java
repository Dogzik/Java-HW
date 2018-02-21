package ru.ifmo.rain.dovzhik.arrayset;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> data;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        comparator = null;
        data = Collections.emptyList();
    }

    public ArraySet(Collection<? extends T> other) {
        comparator = null;
        data = new ArrayList<>(new TreeSet<>(other));
    }

    public ArraySet(Collection<? extends T> other, Comparator<? super T> cmp) {
        comparator = cmp;
        TreeSet<T> tmp = new TreeSet<>(cmp);
        tmp.addAll(other);
        data = new ArrayList<>(tmp);
    }

    private ArraySet(List<T> arr, Comparator<? super T> cmp) {
        comparator = cmp;
        data = arr;
        if (arr instanceof ReversedList) {
            ((ReversedList) arr).reverse();
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public boolean contains(Object o) {
        /*
        try {
            return Collections.binarySearch(data, Objects.requireNonNull(o), comparator) >= 0;
        } catch (ClassCastException e) {
            return false;
        }
        */
        return Collections.binarySearch(data, (T) Objects.requireNonNull(o), comparator) >= 0;
    }

    private T getElem(int ind) {
        return (ind < 0) ? null : data.get(ind);
    }

    private boolean validInd(int x) {
        return 0 <= x && x < size();
    }

    private int indexGetter(T t, int found, int notFound) {
        int res = Collections.binarySearch(data, Objects.requireNonNull(t), comparator);
        if (res < 0) {
            res = -res - 1;
            return validInd(res + notFound) ? res + notFound : -1;
        }
        return validInd(res + found) ? res + found : -1;
    }

    private int lowerInd(T t) {
        return indexGetter(t, -1, -1);
    }

    private int higherInd(T t) {
        return indexGetter(t, 1, 0);
    }

    private int floorInd(T t) {
        return indexGetter(t, 0, -1);
    }

    private int ceilingInd(T t) {
        return indexGetter(t, 0, 0);
    }

    @Override
    public T first() {
        checkNonEmpty();
        return data.get(0);
    }

    @Override
    public T last() {
        checkNonEmpty();
        return data.get(size() - 1);
    }

    private void checkNonEmpty() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public T lower(T t) {
        return getElem(lowerInd(t));
    }

    @Override
    public T higher(T t) {
        return getElem(higherInd(t));
    }

    @Override
    public T floor(T t) {
        return getElem(floorInd(t));
    }

    @Override
    public T ceiling(T t) {
        return getElem(ceilingInd(t));
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ReversedList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int l = fromInclusive ? ceilingInd(fromElement) : higherInd(fromElement);
        int r = toInclusive ? floorInd(toElement) : lowerInd(toElement);
        if (l == -1 || r == -1 || l > r) {
            return Collections.emptyNavigableSet();
        } else {
            return new ArraySet<>(data.subList(l, r + 1), comparator);
        }
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (data.isEmpty()) {
            return Collections.emptyNavigableSet();
        }
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (data.isEmpty()) {
            return Collections.emptyNavigableSet();
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }
}
