package dev.isxander.splitscreen.engine.impl.reparenting.events;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface VanillaWindowFocusEvent {
    Event<VanillaWindowFocusEvent> EVENT = EventFactory.createArrayBacked(VanillaWindowFocusEvent.class,
            (listeners) -> (window, focused) -> {
                for (VanillaWindowFocusEvent listener : listeners) {
                    listener.onFocus(window, focused);
                }
            });

    void onFocus(Window window, boolean focused);
}
