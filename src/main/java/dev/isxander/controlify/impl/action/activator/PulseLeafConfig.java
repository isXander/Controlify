/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.action.Gesture;

import java.util.Optional;

/// Pulses once per instant gesture.
public record PulseLeafConfig(Gesture instantGesture, boolean editable) implements LeafConfig {
	public static final String TYPE_ID = "pulse";
	public static final PulseLeafConfig DEFAULT = new PulseLeafConfig(Gesture.PRESS, false);
	public static final MapCodec<PulseLeafConfig> MAP_CODEC = RecordCodecBuilder.<PulseLeafConfig>mapCodec(instance -> instance.group(
			Gesture.CODEC.optionalFieldOf("gesture", Gesture.PRESS).forGetter(PulseLeafConfig::instantGesture),
			Codec.BOOL.optionalFieldOf("editable", false).forGetter(PulseLeafConfig::editable)
	).apply(instance, PulseLeafConfig::new)).validate(config -> config.instantGesture().kind() == Gesture.Kind.INSTANT
			? DataResult.success(config)
			: DataResult.error(() -> "A pulse activator needs an instant gesture, not " + config.instantGesture().getSerializedName()));

	@Override
	public Kind kind() {
		return Kind.PULSE;
	}

	@Override
	public Optional<Gesture> gesture() {
		return Optional.of(this.instantGesture);
	}

	@Override
	public ActivatorConfigType<LeafSlot, PulseLeafConfig> type() {
		return ActivatorConfigType.PULSE;
	}
}
