package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A supplier that retrieves an {@link InputBinding} for a controller.
 */
public interface InputBindingSupplier {
    /**
     * Retrieves the input binding for the controller, throwing an exception if it does not exist.
     *
     * @param controller the controller to fetch the binding from
     * @throws NullPointerException if the binding does not exist
     * @return the input binding
     */
    default InputBinding on(@NotNull ControllerEntity controller) {
        return Objects.requireNonNull(
                onOrNull(controller),
                () -> "Attempted to fetch " + bindId() + " for controller " + controller.info().uid() + " but it did not exist." +
                        "The binding registry callback may have a filter that did not pass for this controller.");
    }

    /**
     * Retrieves the input binding for the controller, or null if it does not exist.
     *
     * @param controller controller to fetch binding from
     * @return the input binding, or null if it does not exist
     */
    @Nullable InputBinding onOrNull(ControllerEntity controller);

    /**
     * @return id of the binding.
     */
    ResourceLocation bindId();
}
