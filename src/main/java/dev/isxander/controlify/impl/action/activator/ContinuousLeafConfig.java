/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import dev.isxander.controlify.api.action.Gesture;

import java.util.Optional;

/// Mirrors the bound axis value into the continuous channel.
public record ContinuousLeafConfig() implements LeafConfig {
	public static final String TYPE_ID = "continuous";
	public static final ContinuousLeafConfig INSTANCE = new ContinuousLeafConfig();

	@Override
	public Kind kind() {
		return Kind.CONTINUOUS;
	}

	@Override
	public Optional<Gesture> gesture() {
		return Optional.empty();
	}

	@Override
	public boolean editable() {
		return false;
	}

	@Override
	public ActivatorConfigType<LeafSlot, ContinuousLeafConfig> type() {
		return ActivatorConfigType.CONTINUOUS;
	}
}
