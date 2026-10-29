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

/// Holds the latch for the duration of a span gesture, and by default pulses once when it starts.
public record SpanLeafConfig(Gesture spanGesture, boolean editable, boolean pulseOnStart) implements LeafConfig {
	public static final String TYPE_ID = "span";
	public static final SpanLeafConfig DEFAULT = new SpanLeafConfig(Gesture.WHILE_PRESSED, false, true);
	public static final MapCodec<SpanLeafConfig> MAP_CODEC = RecordCodecBuilder.<SpanLeafConfig>mapCodec(instance -> instance.group(
			Gesture.CODEC.optionalFieldOf("gesture", Gesture.WHILE_PRESSED).forGetter(SpanLeafConfig::spanGesture),
			Codec.BOOL.optionalFieldOf("editable", false).forGetter(SpanLeafConfig::editable),
			Codec.BOOL.optionalFieldOf("pulse_on_start", true).forGetter(SpanLeafConfig::pulseOnStart)
	).apply(instance, SpanLeafConfig::new)).validate(config -> config.spanGesture().kind() == Gesture.Kind.SPAN
			? DataResult.success(config)
			: DataResult.error(() -> "A span activator needs a span gesture, not " + config.spanGesture().getSerializedName()));

	@Override
	public Kind kind() {
		return Kind.SPAN;
	}

	@Override
	public Optional<Gesture> gesture() {
		return Optional.of(this.spanGesture);
	}

	@Override
	public ActivatorConfigType<LeafSlot, SpanLeafConfig> type() {
		return ActivatorConfigType.SPAN;
	}
}
