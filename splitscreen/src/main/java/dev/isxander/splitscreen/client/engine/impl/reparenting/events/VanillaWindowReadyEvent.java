package dev.isxander.splitscreen.client.engine.impl.reparenting.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public interface VanillaWindowReadyEvent {
    AtomicBoolean ready = new AtomicBoolean(false);

    Event<VanillaWindowReadyEvent> EVENT = EventFactory.createArrayBacked(VanillaWindowReadyEvent.class, listeners -> () -> {
        ready.set(true);
        for (VanillaWindowReadyEvent listener : listeners) {
            listener.onVanillaWindowReady();
        }
    });

    void onVanillaWindowReady();

    static boolean isReady() {
        return ready.get();
    }
}
