/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.action.Gesture;

import java.util.List;
import java.util.Optional;

/// An activator that reads the action's bindings directly. Every other activator transforms leaves.
public interface LeafConfig extends ActivatorConfig<LeafSlot> {
	Kind kind();

	/// The gesture used unless a binding overrides it; empty for continuous leaves.
	Optional<Gesture> gesture();

	/// Whether a binding may override {@link #gesture()} with another of the same kind.
	boolean editable();

	/// The gesture this leaf uses for the given binding.
	default Optional<Gesture> gestureFor(ActionBinding binding) {
		Optional<Gesture> override = binding.gesture();
		if (this.editable() && override.isPresent() && this.gesture().isPresent()
				&& override.get().kind() == this.gesture().get().kind()) {
			return override;
		}
		return this.gesture();
	}

	@Override
	default LeafSlot create(List<ActionBinding> bindings) {
		return new LeafSlot(this, bindings);
	}

	@Override
	default List<LeafConfig> leaves() {
		return List.of(this);
	}

	enum Kind {
		/// Pulses on an instant gesture. Merged across bindings by summing pulses.
		PULSE,
		/// Holds the latch for a span gesture and pulses once when it starts.
		/// Merged across bindings: active while any binding's span is.
		SPAN,
		/// Mirrors an axis value. Merged across bindings by taking the largest value.
		CONTINUOUS,
	}
}
