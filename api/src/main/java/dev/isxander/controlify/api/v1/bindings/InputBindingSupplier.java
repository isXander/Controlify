package dev.isxander.controlify.api.v1.bindings;

import dev.isxander.controlify.api.CID;
import dev.isxander.controlify.api.MinecraftComponent;
import dev.isxander.controlify.api.v1.ControlifyController;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface InputBindingSupplier {
    /**
     * The unique identifier of the {@link InputBinding} that this
     * supplier fetches from controllers.
     */
    CID bindId();

    /**
     * Retrieves the input binding for the given controller, or null if it was not
     * applied to this specific controller.
     * @param controller the controller to fetch the binding from
     * @return the input binding, or null if it does not exist
     */
    @Nullable InputBinding onOrNull(ControlifyController controller);

    /**
     * Retrieves the input binding for the given controller.
     * @param controller the controller to fetch the binding from
     * @return the input binding
     * @throws NullPointerException if the binding does not exist for this controller
     */
    default @NonNull InputBinding on(ControlifyController controller) {
        InputBinding binding = onOrNull(controller);
        if (binding == null) {
            throw new NullPointerException("Attempted to fetch binding for controller " + controller.uid() + " but it did not exist.");
        }
        return binding;
    }

    /**
     * Returns a glyph icon representing the currently bound input
     * for the currently selected controller.
     * @return the glyph icon component
     */
    @MinecraftComponent Object glyphIcon();
}
