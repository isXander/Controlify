package dev.isxander.controlify.utils;

import java.util.function.Supplier;

public class ResizableRingBuffer<T> {
    private T[] elements;

    private int head;
    private int tail;

    private int size;

    private final Supplier<T> def;

    public ResizableRingBuffer(int initialSize, Supplier<T> def) {
        this.size = initialSize;
        //noinspection unchecked
        this.elements = (T[]) new Object[initialSize];
        this.head = 0;
        this.tail = head + size - 1;
        this.def = def;
    }

    public void push(T element) {
        head = wrapIndex(head + 1);
        tail = wrapIndex(tail + 1);
        elements[tail] = element;
    }

    public T head() {
        return get(head);
    }

    public T head(int offset) {
        return get(wrapIndex(head + offset));
    }

    public T tail() {
        return get(tail);
    }

    public T tail(int offset) {
        return get(wrapIndex(tail - offset));
    }

    private T get(int index) {
        T obj = elements[index];
        if (obj == null) return def.get();
        return obj;
    }

    private int wrapIndex(int index) {
        if (index < 0) index += size;
        if (index >= size) index -= size;
        return index;
    }

    public void setSize(int newSize) {
        if (this.size == newSize)
            return;

        //noinspection unchecked
        T[] newElements = (T[]) new Object[newSize];
        for (int i = 0; i < Math.min(size, newSize); i++) {
            newElements[size - 1 - i] = elements[wrapIndex(tail - i + size)];
        }
        tail = 0;
        head = size - 1;
        this.elements = newElements;
        this.size = newSize;
    }

    public int size() {
        return this.size;
    }
}
