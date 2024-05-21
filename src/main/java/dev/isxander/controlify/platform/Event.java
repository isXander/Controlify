package dev.isxander.controlify.platform;

import dev.isxander.controlify.platform.fabric.FabricBackedEvent;

public interface Event<T> {
    void register(Callback<T> event);

    void invoke(T event);

    static <T> Event<T> createPlatformBackedEvent() {
        return new FabricBackedEvent<>();
    }

    interface Callback<T> {
        void onEvent(T event);
    }
}
