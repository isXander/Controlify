package dev.isxander.controlify.api.v1;

import dev.isxander.controlify.api.v1.bindings.BuiltinBindings;
import dev.isxander.controlify.api.v1.widgetguide.WidgetGuideApi;

import java.util.Optional;

public interface ControlifyApi {
    WidgetGuideApi widgetGuide();

    BuiltinBindings builtinBindings();

    /**
     * Checks if the current input mode is controller mode.
     * This is always true when the user uses "Mixed Input" mode, or true
     * when the controller was the last used input device.
     * @return true if the current input mode is controller mode.
     */
    boolean isControllerMode();

    static Optional<ControlifyApi> get() {
        return Optional.ofNullable(ControlifyApiLoader.get());
    }

    static boolean isControlifyInstalled() {
        return get().isPresent();
    }
}
