package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.impl.controller.input.InputComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record ActionAccessorImpl(Identifier actionId) implements ActionAccessor {
	@Override
	public @Nullable Action onOrNull(InputComponent inputComponent) {
		return inputComponent.getAction(this.actionId);
	}
}
