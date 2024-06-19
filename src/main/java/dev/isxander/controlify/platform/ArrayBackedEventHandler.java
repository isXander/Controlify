package dev.isxander.controlify.platform;

import java.util.ArrayList;
import java.util.List;

public class ArrayBackedEventHandler<T> implements EventHandler<T> {
    private final List<Callback<T>> callbacks = new ArrayList<>();

    @Override
    public void register(Callback<T> event) {
        this.callbacks.add(event);
    }

    @Override
    public void invoke(T event) {
        this.callbacks.forEach(c -> c.onEvent(event));
    }
}
