/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.utils;

/// Conversions between sRGB, linear sRGB and OKLab, all with components as floats.
///
/// sRGB is what colors are written in. Linear sRGB is proportional to emitted light, so it is
/// where light is mixed. OKLab is perceptually uniform, so it is where colors are interpolated.
/// See [Bjorn Ottosson's OKLab post](https://bottosson.github.io/posts/oklab/).
public final class ColorSpaces {
	/// Decodes one sRGB component, 0 to 1, to linear light.
	public static float srgbToLinear(float c) {
		return c <= 0.04045f ? c / 12.92f : (float) Math.pow((c + 0.055f) / 1.055f, 2.4f);
	}

	/// Encodes one linear component, 0 to 1, to sRGB.
	public static float linearToSrgb(float c) {
		return c <= 0.0031308f ? c * 12.92f : 1.055f * (float) Math.pow(c, 1f / 2.4f) - 0.055f;
	}

	/// Writes the OKLab `L, a, b` of a linear sRGB color into `out`.
	public static void linearToOklab(float r, float g, float b, float[] out) {
		float l = (float) Math.cbrt(0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b);
		float m = (float) Math.cbrt(0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b);
		float s = (float) Math.cbrt(0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b);
		out[0] = 0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s;
		out[1] = 1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s;
		out[2] = 0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s;
	}

	/// Writes the linear sRGB of an OKLab color into `out`. Colors outside the sRGB gamut
	/// come out of range; clamp them if needed.
	public static void oklabToLinear(float lightness, float a, float b, float[] out) {
		float l = lightness + 0.3963377774f * a + 0.2158037573f * b;
		float m = lightness - 0.1055613458f * a - 0.0638541728f * b;
		float s = lightness - 0.0894841775f * a - 1.2914855480f * b;
		l = l * l * l;
		m = m * m * m;
		s = s * s * s;
		out[0] = 4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
		out[1] = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
		out[2] = -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s;
	}

	private ColorSpaces() {
	}
}
