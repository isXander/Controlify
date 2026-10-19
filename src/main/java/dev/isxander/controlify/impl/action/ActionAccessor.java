package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.input.InputComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/// An accessor to get an existing action from an input component
public interface ActionAccessor {
	@Nullable Action onOrNull(InputComponent inputComponent);

	Identifier actionId();

	default @Nullable Action onOrNull(ControllerImpl controller) {
		return onOrNull(controller.input().orElseThrow());
	}

	default Action on(InputComponent inputComponent) {
		Action action = onOrNull(inputComponent);
		if (action == null) throw new NullPointerException("No action bound to input component " + actionId());
		return action;
	}

	default Action on(ControllerImpl controller) {
		return on(controller.input().orElseThrow());
	}
}
