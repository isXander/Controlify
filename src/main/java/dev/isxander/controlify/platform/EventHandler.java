package dev.isxander.controlify.platform;

public interface EventHandler<T> {
    void register(Callback<T> event);

    void invoke(T event);

    static <T> EventHandler<T> createPlatformBackedEvent() {
        return new ArrayBackedEventHandler<>();
    }

    @FunctionalInterface
    interface Callback<T> {
        void onEvent(T event);
    }
}
