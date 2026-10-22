package dev.isxander.controlify.api.controller.input;

import dev.isxander.controlify.api.action.Action;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/// Public input capabilities and current actions of a controller.
public interface InputComponent {
	/// Null when unavailable. Resolve again after profile changes, which can replace actions.
	@Nullable Action getAction(Identifier actionId);

	boolean isGamepad();
}
