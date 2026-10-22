package dev.isxander.controlify.api.ingame;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.ingame.InGameInputHandler;

public final class InGameEvents {
	/// Modifies the final look impulse at the existing pre-application hook.
	public static final EventSource<LookInputModifier> LOOK_INPUT_MODIFIER = InGameInputHandler.Events.LOOK_INPUT_MODIFIER;

	private InGameEvents() {}
}
