/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.ArrayList;
import java.util.List;

/// A piecewise function of time, defined by points `[seconds, strength]` or
/// `[seconds, strength, easing]`. The easing of a point shapes the segment arriving at it.
/// Two points at the same time make a step: from that time on, the later point applies.
///
/// @param points at least one point, sorted by time
public record Envelope(List<Point> points) {
	public static final Codec<Envelope> CODEC = Point.CODEC.listOf().comapFlatMap(Envelope::validate, Envelope::points);

	public Envelope {
		points = List.copyOf(points);
	}

	private static DataResult<Envelope> validate(List<Point> points) {
		if (points.isEmpty()) {
			return DataResult.error(() -> "An envelope needs at least one point");
		}
		float previous = 0f;
		for (Point point : points) {
			if (!Float.isFinite(point.time()) || point.time() < previous) {
				return DataResult.error(() -> "Envelope times must be finite, not negative and not decreasing");
			}
			if (!Float.isFinite(point.strength())) {
				return DataResult.error(() -> "Envelope strengths must be finite");
			}
			previous = point.time();
		}
		return DataResult.success(new Envelope(points));
	}

	/// The time of the last point, which is when a one-shot with this envelope ends.
	public float end() {
		return this.points.getLast().time();
	}

	/// The strength at `seconds`. Before the first point the first strength applies, and after
	/// the last point the last strength is held.
	public float at(float seconds) {
		int from = -1;
		for (int i = 0; i < this.points.size() && this.points.get(i).time() <= seconds; i++) {
			from = i;
		}
		if (from < 0) {
			return this.points.getFirst().strength();
		}
		if (from == this.points.size() - 1) {
			return this.points.get(from).strength();
		}

		Point a = this.points.get(from);
		Point b = this.points.get(from + 1);
		float progress = (seconds - a.time()) / (b.time() - a.time());
		return b.easing().lerp(a.strength(), b.strength(), progress);
	}

	public record Point(float time, float strength, Easing easing) {
		static final Codec<Point> CODEC = new Codec<>() {
			@Override
			public <T> DataResult<Pair<Point, T>> decode(DynamicOps<T> ops, T input) {
				return ops.getStream(input).flatMap(stream -> {
					List<T> elements = stream.toList();
					if (elements.size() != 2 && elements.size() != 3) {
						return DataResult.<Point>error(() -> "An envelope point is [seconds, strength] or [seconds, strength, easing]");
					}

					return ops.getNumberValue(elements.get(0)).flatMap(time ->
							ops.getNumberValue(elements.get(1)).flatMap(strength -> {
								DataResult<Easing> easing = elements.size() == 3
										? Easing.CODEC.parse(ops, elements.get(2))
										: DataResult.success(Easing.LINEAR);
								return easing.map(e -> new Point(time.floatValue(), strength.floatValue(), e));
							})
					);
				}).map(point -> Pair.of(point, ops.empty()));
			}

			@Override
			public <T> DataResult<T> encode(Point point, DynamicOps<T> ops, T prefix) {
				List<T> elements = new ArrayList<>();
				elements.add(ops.createFloat(point.time()));
				elements.add(ops.createFloat(point.strength()));
				if (point.easing() != Easing.LINEAR) {
					elements.add(ops.createString(point.easing().getSerializedName()));
				}
				return ops.mergeToPrimitive(prefix, ops.createList(elements.stream()));
			}
		};
	}
}
