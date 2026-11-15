/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;

/// A repeating wave ("low-frequency oscillator") that a track's strength is multiplied by.
/// It swings between `min` and `max` once every `period` seconds.
///
/// @param on for a square wave, how many seconds of each period it spends at `max`.
///           Defaults to half the period.
public record Lfo(float period, Shape shape, Optional<Float> on, float min, float max) {
	public static final Codec<Lfo> CODEC = RecordCodecBuilder.<Lfo>create(instance -> instance.group(
			Codec.FLOAT.fieldOf("period").forGetter(Lfo::period),
			Shape.CODEC.optionalFieldOf("shape", Shape.SQUARE).forGetter(Lfo::shape),
			Codec.FLOAT.optionalFieldOf("on").forGetter(Lfo::on),
			Codec.FLOAT.optionalFieldOf("min", 0f).forGetter(Lfo::min),
			Codec.FLOAT.optionalFieldOf("max", 1f).forGetter(Lfo::max)
	).apply(instance, Lfo::new)).validate(lfo -> {
		if (!Float.isFinite(lfo.period) || lfo.period <= 0f) {
			return DataResult.error(() -> "An lfo period must be positive");
		}
		if (lfo.on.isPresent() && (!Float.isFinite(lfo.on.get()) || lfo.on.get() < 0f)) {
			return DataResult.error(() -> "An lfo 'on' time cannot be negative");
		}
		if (!Float.isFinite(lfo.min) || !Float.isFinite(lfo.max)) {
			return DataResult.error(() -> "An lfo's min and max must be finite");
		}
		return DataResult.success(lfo);
	});

	public float at(float seconds) {
		float phase = (seconds % this.period) / this.period;
		float wave = switch (this.shape) {
			case SQUARE -> phase * this.period < this.on.orElse(this.period / 2f) ? 1f : 0f;
			case SAW -> phase;
			case TRIANGLE -> 1f - Math.abs(phase * 2f - 1f);
			case SINE -> 0.5f - 0.5f * (float) Math.cos(phase * 2d * Math.PI);
		};
		return this.min + (this.max - this.min) * wave;
	}

	public enum Shape implements StringRepresentable {
		/// At `max` for the first `on` seconds of each period, then at `min`.
		SQUARE("square"),
		/// Rises steadily from `min` to `max`, then drops back.
		SAW("saw"),
		/// Rises steadily from `min` to `max`, then falls steadily back.
		TRIANGLE("triangle"),
		/// Swings smoothly from `min` up to `max` and back.
		SINE("sine");

		public static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);

		private final String name;

		Shape(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
