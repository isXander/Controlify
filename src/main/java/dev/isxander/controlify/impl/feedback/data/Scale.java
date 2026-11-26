/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.feedback.Playback;
import net.minecraft.util.Mth;

import java.util.List;

/// Multiplies a track by one of the event's named params.
///
/// Written as just the param name to use the value as it is, or as an object to remap it
/// first: the param is clamped to the `from` range, eased, and stretched onto the `to` range.
/// A param the event does not provide counts as zero.
///
/// @param from the `[min, max]` range of the param
/// @param to   the `[min, max]` strength that range maps onto
public record Scale(String param, List<Float> from, List<Float> to, Easing ease) {
	private static final List<Float> UNIT = List.of(0f, 1f);

	private static final Codec<Scale> OBJECT_CODEC = RecordCodecBuilder.<Scale>create(instance -> instance.group(
			Codec.STRING.fieldOf("param").forGetter(Scale::param),
			Codec.FLOAT.listOf().optionalFieldOf("from", UNIT).forGetter(Scale::from),
			Codec.FLOAT.listOf().optionalFieldOf("to", UNIT).forGetter(Scale::to),
			Easing.CODEC.optionalFieldOf("ease", Easing.LINEAR).forGetter(Scale::ease)
	).apply(instance, Scale::new)).validate(scale -> {
		if (scale.from.size() != 2 || scale.to.size() != 2) {
			return DataResult.error(() -> "A scale's 'from' and 'to' must each be [min, max]");
		}
		if (scale.from.get(0).equals(scale.from.get(1))) {
			return DataResult.error(() -> "A scale's 'from' range cannot be empty");
		}
		return DataResult.success(scale);
	});

	public static final Codec<Scale> CODEC = Codec.either(Codec.STRING, OBJECT_CODEC).xmap(
			either -> either.map(Scale::of, scale -> scale),
			scale -> scale.isPlain() ? Either.left(scale.param) : Either.right(scale)
	);

	public Scale {
		from = List.copyOf(from);
		to = List.copyOf(to);
	}

	/// A scale that uses the param's value as it is.
	public static Scale of(String param) {
		return new Scale(param, UNIT, UNIT, Easing.LINEAR);
	}

	private boolean isPlain() {
		return this.from.equals(UNIT) && this.to.equals(UNIT) && this.ease == Easing.LINEAR;
	}

	public float apply(Playback<?> playback) {
		float value = playback.param(this.param, 0f);

		float progress = Math.clamp(Mth.inverseLerp(value, this.from.get(0), this.from.get(1)), 0f, 1f);
		return Mth.lerp(this.ease.apply(progress), this.to.get(0), this.to.get(1));
	}
}
