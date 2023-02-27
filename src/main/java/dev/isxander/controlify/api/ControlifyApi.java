package dev.isxander.controlify.api;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.bind.ControlifyBindingsApi;
import dev.isxander.controlify.controller.Controller;
import org.jetbrains.annotations.NotNull;

/**
 * Interface with Controlify in a manner where you don't need to worry about updates
 * breaking! This is the recommended way to interact with Controlify.
 */
public interface ControlifyApi {
    /**
     * @return the controller currently in use. If disabled, this will return {@link Controller#DUMMY}
     */
    @NotNull Controller<?, ?> currentController();

    @NotNull InputMode currentInputMode();
    void setInputMode(@NotNull InputMode mode);

    static ControlifyApi get() {
        return Controlify.instance();
    }
}
