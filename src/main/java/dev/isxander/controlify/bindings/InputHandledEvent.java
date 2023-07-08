package dev.isxander.controlify.bindings;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface InputHandledEvent {
    void onInputHandled();

    Event<InputHandledEvent> EVENT = EventFactory.createArrayBacked(InputHandledEvent.class, (listeners) -> () -> {
        for (InputHandledEvent listener : listeners) {
            listener.onInputHandled();
        }
    });
}
