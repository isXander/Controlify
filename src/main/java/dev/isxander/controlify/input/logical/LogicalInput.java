package dev.isxander.controlify.input.logical;

import net.minecraft.resources.Identifier;

import java.util.Set;

/// A reference to a logical button, axis, or sensor.
///
/// A logical input's domain is the type of input, meaning that
/// a logical input that refers to the same physical inputs can be
/// safely duplicated between continuous and digital signals.
///
/// Most of the time this will refer to a single physical button or axis,
/// like the `A` button or `Left Stick` left, but it may also refer to
/// a chord, like `A` + `B`.
public sealed interface LogicalInput {
	/// A logical input with a direct relationship with a physical input.
	record Single(Identifier physicalInput) implements LogicalInput {

	}

	/// A logical input that triggers only after all modifiers are already pressed.
	record Modified(Set<Identifier> modifiers, Identifier trigger) implements LogicalInput {

	}
}
