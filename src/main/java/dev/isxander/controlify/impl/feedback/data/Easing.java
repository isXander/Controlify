/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback.data;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.impl.utils.Easings;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

/// How an envelope moves towards a point from the one before it.
public enum Easing implements StringRepresentable {
	LINEAR("linear") {
		@Override
		public float apply(float t) {
			return t;
		}
	},
	IN_QUAD("in_quad") {
		@Override
		public float apply(float t) {
			return (float) Easings.easeInQuad(t);
		}
	},
	OUT_QUAD("out_quad") {
		@Override
		public float apply(float t) {
			return (float) Easings.easeOutQuad(t);
		}
	},
	IN_OUT_QUAD("in_out_quad") {
		@Override
		public float apply(float t) {
			return t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
		}
	},
	IN_SINE("in_sine") {
		@Override
		public float apply(float t) {
			return (float) Easings.easeInSine(t);
		}
	},
	OUT_EXPO("out_expo") {
		@Override
		public float apply(float t) {
			return (float) Easings.easeOutExpo(t);
		}
	};

	public static final Codec<Easing> CODEC = StringRepresentable.fromEnum(Easing::values);

	private final String name;

	Easing(String name) {
		this.name = name;
	}

	/// @param t progress through the segment, 0 to 1
	/// @return eased progress, 0 to 1
	public abstract float apply(float t);

	public float lerp(float from, float to, float t) {
		return Mth.lerp(this.apply(t), from, to);
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
