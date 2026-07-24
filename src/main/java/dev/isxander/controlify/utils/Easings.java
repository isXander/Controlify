/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils;

import net.minecraft.util.Mth;

import java.util.function.UnaryOperator;

public class Easings {
	public static double easeInSine(double t) {
		return 1 - Mth.cos((float) ((t * Math.PI) / 2));
	}

	public static double easeInQuad(double t) {
		return t * t;
	}

	public static double easeOutQuad(double t) {
		return 1 - (1 - t) * (1 - t);
	}

	public static double easeOutExpo(double t) {
		return t == 1 ? 1 : 1 - (float) Math.pow(2, -10 * t);
	}

	public static UnaryOperator<Float> toFloat(UnaryOperator<Double> easing) {
		return f -> easing.apply(f.doubleValue()).floatValue();
	}
}
