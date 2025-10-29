package dev.isxander.controlify.input.pipeline;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface EventBuffer<T> {
    @Nullable T poll();

    @Nullable T peek();

    boolean isEmpty();

    default List<T> drain() {
        var result = new ArrayList<T>();
        for (T e; (e = poll()) != null; ) {
            result.add(e);
        }
        return result;
    }

    default void drainTo(EventSink<? super T> sink) {
        for (T e; (e = poll()) != null; ) {
            sink.accept(e);
        }
    }
}
