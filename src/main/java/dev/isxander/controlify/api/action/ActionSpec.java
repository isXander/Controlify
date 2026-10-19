package dev.isxander.controlify.api.action;

import dev.isxander.controlify.impl.action.ActionSpecBuilder;
import dev.isxander.controlify.impl.action.ActionSpecBuilderImpl;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

/// Immutable information about an {@link Action}.
/// @param id the unique identifier for the action
/// @param name the display name for the action
/// @param description the display description for the action
/// @param category the category for the action
/// @param contexts the set of context IDs that this action should work in
/// @param shouldExpose predicate deciding if the action should be visible in the settings and available
///                     to bind for the current controller
public record ActionSpec(
		Identifier id,
		Component name,
		Component description,
		Component category,
		Set<Identifier> contexts,
		Predicate<ControllerImpl> shouldExpose
) implements Comparable<ActionSpec> {
	@Override
	public int compareTo(ActionSpec o) {
		return Comparator.comparing(ActionSpec::id)
				.compare(this, o);
	}

	public static ActionSpecBuilder builder() {
		return new ActionSpecBuilderImpl();
	}
}
