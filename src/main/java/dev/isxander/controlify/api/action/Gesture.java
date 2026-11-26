/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.EnumSet;
import java.util.Set;

/// What a binding's input must do to activate an action.
/// Instant gestures happen at a single moment and can only produce pulses.
/// Span gestures have a start and an end, so they can also hold a latch.
public enum Gesture implements StringRepresentable {
	PRESS("press", Kind.INSTANT, Outcome.ALL, true),
	RELEASE("release", Kind.INSTANT, Outcome.ALL, false),
	TAP("tap", Kind.INSTANT, EnumSet.of(Outcome.TAP), false),
	LONG_PRESS("long_press", Kind.INSTANT, EnumSet.of(Outcome.LONG), true),
	/// A release with no GUI navigation since the press.
	GUI_PRESS("gui_press", Kind.INSTANT, Outcome.ALL, false),

	HOLD("hold", Kind.SPAN, Outcome.ALL, true),
	LONG_HOLD("long_hold", Kind.SPAN, EnumSet.of(Outcome.LONG), true);

	public static final Codec<Gesture> CODEC = StringRepresentable.fromEnum(Gesture::values);

	private final String id;
	private final Kind kind;
	private final Set<Outcome> outcomes;
	private final boolean firesBeforeRelease;

	Gesture(String id, Kind kind, Set<Outcome> outcomes, boolean firesBeforeRelease) {
		this.id = id;
		this.kind = kind;
		this.outcomes = Set.copyOf(outcomes);
		this.firesBeforeRelease = firesBeforeRelease;
	}

	public Kind kind() {
		return this.kind;
	}

	/// The ways a single actuation can end that make this gesture fire.
	/// Two gestures on the same input conflict when these overlap.
	public Set<Outcome> outcomes() {
		return this.outcomes;
	}

	/// Whether this gesture can fire while the input is still held, before anything later
	/// can cancel it. Such gestures still fire when their button is used as a chord modifier.
	public boolean firesBeforeRelease() {
		return this.firesBeforeRelease;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}

	public enum Kind {
		INSTANT,
		SPAN,
	}

	/// The mutually exclusive ways a single press of an input can end.
	/// Relies on the hold duration exceeding the tap duration.
	public enum Outcome {
		/// A short press.
		TAP,
		/// Longer than a tap, released before the hold duration.
		MEDIUM,
		/// Reached the long press duration.
		LONG;

		static final Set<Outcome> ALL = EnumSet.allOf(Outcome.class);
	}
}
