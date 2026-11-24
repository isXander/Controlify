/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.isxander.controlify.impl.utils.ColorSpaces;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/// The color of a light track as a function of its strength, defined by stops
/// `[color, position]` or `[color, position, easing]` with positions from 0 to 1. Colors are
/// `#RRGGBB`, or `#RRGGBBAA` to make the stop partly transparent.
///
/// Written as a single color, it is shorthand for a gradient from that color fully
/// transparent at 0 to that color at 1, so strength works as opacity.
///
/// Between stops, colors are interpolated in OKLab with premultiplied alpha, so midpoints
/// look like midpoints and a transparent stop does not tint its neighbour. As with an
/// envelope, the easing of a stop shapes the segment arriving at it, and two stops at the
/// same position make a step.
public final class ColorGradient {
	private static final Codec<ColorGradient> STOPS_CODEC = Stop.CODEC.listOf()
			.comapFlatMap(ColorGradient::validate, ColorGradient::stops);

	public static final Codec<ColorGradient> CODEC = Codec.either(Color.CODEC, STOPS_CODEC).xmap(
			either -> either.map(ColorGradient::fadeIn, gradient -> gradient),
			Either::right
	);

	private final List<Stop> stops;
	/// Per stop, the OKLab color multiplied by alpha.
	private final float[][] premultipliedLab;
	/// Per stop, the sRGB color.
	private final float[][] srgb;

	/// @param stops at least one, sorted by position
	public ColorGradient(List<Stop> stops) {
		this.stops = List.copyOf(stops);
		this.premultipliedLab = new float[this.stops.size()][];
		this.srgb = new float[this.stops.size()][];

		for (int i = 0; i < this.stops.size(); i++) {
			Color color = this.stops.get(i).color();

			this.srgb[i] = color.srgb();

			this.premultipliedLab[i] = new float[3];
			ColorSpaces.linearToOklab(
					ColorSpaces.srgbToLinear(this.srgb[i][0]),
					ColorSpaces.srgbToLinear(this.srgb[i][1]),
					ColorSpaces.srgbToLinear(this.srgb[i][2]),
					this.premultipliedLab[i]
			);

			for (int c = 0; c < 3; c++) {
				this.premultipliedLab[i][c] *= color.alpha();
			}
		}
	}

	/// A gradient from `color` fully transparent to `color`.
	public static ColorGradient fadeIn(Color color) {
		return new ColorGradient(List.of(
				new Stop(color.withAlpha(0f), 0f, Easing.LINEAR),
				new Stop(color, 1f, Easing.LINEAR)
		));
	}

	private static DataResult<ColorGradient> validate(List<Stop> stops) {
		if (stops.isEmpty()) {
			return DataResult.error(() -> "A color gradient needs at least one stop");
		}
		float previous = 0f;
		for (Stop stop : stops) {
			if (!(stop.position() >= previous && stop.position() <= 1f)) {
				return DataResult.error(() -> "Color gradient positions must be from 0 to 1 and not decreasing");
			}
			previous = stop.position();
		}
		return DataResult.success(new ColorGradient(stops));
	}

	public List<Stop> stops() {
		return this.stops;
	}

	/// Writes the sRGB color and alpha at `t` into `out`, as `[r, g, b, a]` from 0 to 1.
	/// Below the first stop the first color applies, and above the last the last.
	public void sample(float t, float[] out) {
		int from = -1;
		for (int i = 0; i < this.stops.size() && this.stops.get(i).position() <= t; i++) {
			from = i;
		}
		if (from < 0) {
			this.writeStop(0, out);
			return;
		}
		if (from == this.stops.size() - 1) {
			this.writeStop(from, out);
			return;
		}

		Stop a = this.stops.get(from);
		Stop b = this.stops.get(from + 1);

		float progress = b.easing().apply(Mth.inverseLerp(t, a.position(), b.position()));
		float alpha = Mth.lerp(progress, a.color().alpha(), b.color().alpha());
		if (alpha <= 0f) {
			out[0] = out[1] = out[2] = out[3] = 0f;
			return;
		}

		float[] labA = this.premultipliedLab[from];
		float[] labB = this.premultipliedLab[from + 1];
		ColorSpaces.oklabToLinear(
				Mth.lerp(progress, labA[0], labB[0]) / alpha,
				Mth.lerp(progress, labA[1], labB[1]) / alpha,
				Mth.lerp(progress, labA[2], labB[2]) / alpha,
				out
		);
		for (int i = 0; i < 3; i++) {
			out[i] = ColorSpaces.linearToSrgb(Math.clamp(out[i], 0f, 1f));
		}
		out[3] = alpha;
	}

	private void writeStop(int index, float[] out) {
		float alpha = this.stops.get(index).color().alpha();
		if (alpha <= 0f) {
			out[0] = out[1] = out[2] = out[3] = 0f;
			return;
		}
		System.arraycopy(this.srgb[index], 0, out, 0, 3);
		out[3] = alpha;
	}

	/// An sRGB color with alpha, from `#RRGGBB` or `#RRGGBBAA`.
	///
	/// @param rgb   `0xRRGGBB`
	/// @param alpha 0 to 1
	public record Color(int rgb, float alpha) {
		public static final Codec<Color> CODEC = Codec.STRING.comapFlatMap(Color::parse, Color::toString);

		public Color {
			rgb &= 0xFFFFFF;
		}

		public static DataResult<Color> parse(String string) {
			boolean valid = string.startsWith("#") && (string.length() == 7 || string.length() == 9);
			if (valid) {
				try {
					long value = Long.parseLong(string.substring(1), 16);
					return DataResult.success(string.length() == 7
							? new Color((int) value, 1f)
							: new Color((int) (value >> 8), (value & 0xFF) / 255f));
				} catch (NumberFormatException ignored) {
					// falls through to the error
				}
			}
			return DataResult.error(() -> "Not a color: '" + string + "'. Use #RRGGBB or #RRGGBBAA");
		}

		public Color withAlpha(float alpha) {
			return new Color(this.rgb, alpha);
		}

		/// The components, 0 to 1, ignoring alpha.
		float[] srgb() {
			return new float[] { ((this.rgb >> 16) & 0xFF) / 255f, ((this.rgb >> 8) & 0xFF) / 255f, (this.rgb & 0xFF) / 255f };
		}

		@Override
		public @NonNull String toString() {
			String hex = String.format(Locale.ROOT, "#%06x", this.rgb);
			return this.alpha >= 1f ? hex : hex + String.format(Locale.ROOT, "%02x", Math.round(this.alpha * 255f));
		}
	}

	/// @param position 0 to 1, the track strength at which this color applies
	public record Stop(Color color, float position, Easing easing) {
		static final Codec<Stop> CODEC = new Codec<>() {
			@Override
			public <T> DataResult<Pair<Stop, T>> decode(DynamicOps<T> ops, T input) {
				return ops.getStream(input).flatMap(stream -> {
					List<T> elements = stream.toList();
					if (elements.size() != 2 && elements.size() != 3) {
						return DataResult.<Stop>error(() -> "A color stop is [color, position] or [color, position, easing]");
					}
					return Color.CODEC.parse(ops, elements.get(0)).flatMap(color ->
							ops.getNumberValue(elements.get(1)).flatMap(position -> {
								DataResult<Easing> easing = elements.size() == 3
										? Easing.CODEC.parse(ops, elements.get(2))
										: DataResult.success(Easing.LINEAR);
								return easing.map(e -> new Stop(color, position.floatValue(), e));
							})
					);
				}).map(stop -> Pair.of(stop, ops.empty()));
			}

			@Override
			public <T> DataResult<T> encode(Stop stop, DynamicOps<T> ops, T prefix) {
				List<T> elements = new ArrayList<>();
				elements.add(ops.createString(stop.color().toString()));
				elements.add(ops.createFloat(stop.position()));
				if (stop.easing() != Easing.LINEAR) {
					elements.add(ops.createString(stop.easing().getSerializedName()));
				}
				return ops.mergeToPrimitive(prefix, ops.createList(elements.stream()));
			}
		};
	}
}
