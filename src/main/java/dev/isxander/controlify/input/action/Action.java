package dev.isxander.controlify.input.action;

import dev.isxander.controlify.input.logical.LogicalInput;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/// An action is bound to a logical input and has one or more activators
/// that determine how its output channels respond to input signals.
public interface Action {

	ActionSpec spec();

	Channel.Pulse pulse();
	Channel.Latch latch();
	Channel.Continuous continuous();

	Component inputGlyph();

	void setBoundInput(@Nullable LogicalInput input);

	@Nullable LogicalInput boundInput();

	@Nullable LogicalInput defaultInput();
}
