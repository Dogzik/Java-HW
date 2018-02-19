package ru.ifmo.rain.dovzhik.arrayset;

import java.util.AbstractList;
import java.util.List;

public class ReversedList<T> extends AbstractList<T> {
    private boolean reversed;
    private List<T> data;

    public int size() {
        return data.size();
    }

    public void reverse() {
        reversed = !reversed;
    }
    
    public ReversedList(List<T> other) {
        data = other;
    }

    @Override
    public T get(int index) {
        return reversed ? data.get(size() - 1 - index) : data.get(index);
    }
}
