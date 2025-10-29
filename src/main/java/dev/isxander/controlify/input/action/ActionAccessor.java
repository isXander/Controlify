package dev.isxander.controlify.input.action;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.input.InputComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * An accessor to get an existing action handle from an input component.
 * @param <T> the type of action handle
 */
public interface ActionAccessor<T extends ActionHandle> {
    @Nullable T onOrNull(InputComponent inputComponent);

    ResourceLocation actionId();

    default @Nullable T onOrNull(ControllerEntity controller) {
        return onOrNull(controller.input().orElseThrow());
    }

    default T on(InputComponent inputComponent) {
        T action = onOrNull(inputComponent);
        if (action == null) throw new NullPointerException("No action bound to handle " + actionId());
        return action;
    }

    default T on(ControllerEntity controller) {
        return on(controller.input().orElseThrow());
    }
}
