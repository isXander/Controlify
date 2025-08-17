package dev.isxander.controlify.input.pipeline;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

public class SimpleEventBuffer<T> implements EventBuffer<T>, EventSink<T> {
    private final Queue<T> queue;

    public SimpleEventBuffer() {
        this.queue = new ArrayDeque<>();
    }

    @Override
    public void accept(T event) {
        this.queue.add(event);
    }

    @Override
    public @Nullable T poll() {
        return this.queue.poll();
    }

    @Override
    public @Nullable T peek() {
        return this.queue.peek();
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }
}
