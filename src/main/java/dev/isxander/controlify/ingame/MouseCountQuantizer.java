/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.ingame;

/** Whole mouse counts, with fractional input carried between render frames. */
final class MouseCountQuantizer {

	private double remainderX, remainderY;
	private double previousScale = Double.NaN;

	record Turn(double x, double y) { }

	static double mouseScale(double sensitivity, boolean scoping) {
		// Match MouseHandler, including its float constants promoted to double.
		double base = sensitivity * 0.6F + 0.2F;
		return base * base * base * (scoping ? 1 : 8);
	}

	Turn quantize(double x, double y, double scale, float pitch) {
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(scale)
				|| scale <= 0 || !Float.isFinite(pitch)) {
			reset();
			return new Turn(0, 0);
		}
		if (scale != previousScale) {
			reset();
			previousScale = scale;
		}
		if (pitch >= 90 && y > 0 || pitch <= -90 && y < 0) {
			remainderY = 0;
			y = 0;
		}

		double countsX = x / scale + remainderX;
		double countsY = y / scale + remainderY;
		if (!Double.isFinite(countsX) || !Double.isFinite(countsY)) {
			reset();
			return new Turn(0, 0);
		}
		double wholeX = x == 0 ? 0 : Math.rint(countsX);
		double wholeY = y == 0 ? 0 : Math.rint(countsY);
		remainderX = countsX - wholeX;
		remainderY = countsY - wholeY;
		Turn turn = new Turn(wholeX * scale, wholeY * scale);

		float nextPitch = pitch + (float) turn.y() * 0.15F;
		// Discard input clipped by vanilla, but keep tiny inward input at the limit.
		if (turn.y() > 0 && nextPitch >= 90 || turn.y() < 0 && nextPitch <= -90) remainderY = 0;
		return turn;
	}

	void reset() {
		remainderX = remainderY = 0;
		previousScale = Double.NaN;
	}
}
