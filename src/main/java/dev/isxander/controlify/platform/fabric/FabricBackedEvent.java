package dev.isxander.controlify.platform.fabric;

import dev.isxander.controlify.platform.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class FabricBackedEvent<T> implements Event<T> {
    private final net.fabricmc.fabric.api.event.Event<Callback<T>> backedEvent;

    public FabricBackedEvent() {
        this.backedEvent = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
            for (Callback<T> callback : callbacks) {
                callback.onEvent(event);
            }
        });
    }

    @Override
    public void register(Callback<T> event) {
        this.backedEvent.register(event);
    }

    @Override
    public void invoke(T event) {
        this.backedEvent.invoker().onEvent(event);
    }
}
