package info.kgeorgiy.ja.minko.arrayset;

import java.util.SortedSet;
import java.util.List;
import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.AbstractSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {

    private final List<T> list;
    private final Comparator<T> comparator;

    public ArraySet() {
        this.list = Collections.emptyList();
        this.comparator = null;
    }

    public ArraySet(Collection<T> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<T> collection, Comparator<T> comparator) {
        Set<T> set = new TreeSet<>(comparator);
        set.addAll(collection);
        this.list = List.copyOf(set);
        this.comparator = comparator;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    private int getIndex(T element) {
        int i = Collections.binarySearch(list, element, comparator);
        if (i < 0) {
            return -i - 1;
        } else {
            return i;
        }
    }

    private SortedSet<T> getSet(T fromElement, T toElement, int shape) {
        int from = getIndex(fromElement);
        int to = getIndex(toElement) + shape;

        if (from == -1 || to == -1) {
            return new ArraySet<>();
        }

        return new ArraySet<>(list.subList(from, to), comparator);
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (comparator != null && comparator.compare(fromElement, toElement) >= 0) {
            throw new IllegalArgumentException("Incorrect order of interval boundaries");
        }
        return getSet(fromElement, toElement, 0);
    }

    private SortedSet<T> positionSet(T Element, String position) {
        if (!isEmpty()) {
            // :NOTE: string switch
            return switch (position) {
                case "head" -> getSet(first(), Element, 0);
                case "tail" -> getSet(Element, last(), 1);
                default -> throw new UnsupportedOperationException("Unsupported token of position exception");
            };
        } else {
            return new ArraySet<>();
        }
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return positionSet(toElement, "head");
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return positionSet(fromElement, "tail");
    }

    private T getElement(String number) {
        if (!isEmpty()) {
            return switch (number) {
                case "first" -> list.get(0);
                case "last" -> list.get(list.size() - 1);
                default -> throw new UnsupportedOperationException("Unsupported token of number exception");
            };
        } else {
            throw new NoSuchElementException("Try to get " + number + " element from empty");
        }
    }

    @Override
    public T first() {
        return getElement("first");
    }

    @Override
    public T last() {
        return getElement("last");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(list, (T) o, comparator) >= 0;
    }
}
