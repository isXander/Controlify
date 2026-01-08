package dev.isxander.controlify.api;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import org.jspecify.annotations.NonNull;

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
     * The impl that is currently enabled and in use.
     * If there is no impl disconnected or disabled, this will return {@link Optional#empty()}.
     * This is the impl that is used for {@link dev.isxander.controlify.api.event.ControlifyEvents#ACTIVE_CONTROLLER_TICKED}
     */
    @NonNull Optional<ControllerEntity> getCurrentController();

    /**
     * The last input received: a impl or keyboard/mouse.
     */
    @NonNull InputMode currentInputMode();
    boolean setInputMode(@NonNull InputMode mode);

    default void playRumbleEffect(@NonNull RumbleSource rumbleSource, @NonNull RumbleEffect rumbleEffect) {
        getCurrentController()
                .flatMap(ControllerEntity::rumble)
                .ifPresent(r -> r.rumbleManager().play(rumbleSource, rumbleEffect));
    }

    static ControlifyApi get() {
        return Controlify.instance();
    }
}
