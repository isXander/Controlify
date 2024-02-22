package dev.isxander.controlify.api;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.ControllerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Interface with Controlify in a manner where you don't need to worry about updates
 * breaking! This is the recommended way to interact with Controlify.
 * <p>
 * Alternatively, to use Controlify directly, you can use {@link Controlify#instance()}. Though
 * beware, things may break at any time!
 * <p>
 * Anything that is asked for from this API is safe to use, even if it is not in the API package.
 */
public interface ControlifyApi {
    /**
     * The controller that is currently enabled and in use.
     * If there is no controller disconnected or disabled, this will return {@link Optional#empty()}.
     * This is the controller that is used for {@link dev.isxander.controlify.api.event.ControlifyEvents#ACTIVE_CONTROLLER_TICKED}
     */
    @NotNull Optional<ControllerEntity> getCurrentController();

    /**
     * The last input received: a controller or keyboard/mouse.
     */
    @NotNull InputMode currentInputMode();
    boolean setInputMode(@NotNull InputMode mode);

    static ControlifyApi get() {
        return Controlify.instance();
    }
}
