package dev.isxander.controlify.input.action;

import dev.isxander.controlify.input.InputComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record ActionAccessorImpl(Identifier actionId) implements ActionAccessor {
	@Override
	public @Nullable Action onOrNull(InputComponent inputComponent) {
		return inputComponent.getAction(this.actionId);
	}
}
