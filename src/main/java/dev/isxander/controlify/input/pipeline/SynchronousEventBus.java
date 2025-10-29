package dev.isxander.controlify.input.pipeline;

import org.jetbrains.annotations.Nullable;

public class SynchronousEventBus<T> extends SimpleEventBus<T> {
    private @Nullable Thread thread;

    private SynchronousEventBus(@Nullable Thread thread) {
        this.thread = thread;
    }

    public static <T> SynchronousEventBus<T> createForCurrentThread() {
        return new SynchronousEventBus<>(Thread.currentThread());
    }

    public static <T> SynchronousEventBus<T> createForThread(Thread thread) {
        return new SynchronousEventBus<>(thread);
    }

    public static <T> SynchronousEventBus<T> createForFirstThread() {
        return new SynchronousEventBus<>(null);
    }

    @Override
    public void accept(T event) {
        if (this.thread == null) {
            this.thread = Thread.currentThread();
        }

        if (Thread.currentThread() != this.thread) {
            String message =
                    "ThreadedEventBus: accept must be called on the owner thread (" +
                    thread.getName() + "), but was called on " +
                    Thread.currentThread().getName() + ".";
            throw new IllegalStateException(message);
        }

        super.accept(event);
    }
}
