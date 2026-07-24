/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.utils;

import net.minecraft.util.Mth;

public final class MthExt {
	private MthExt() {
	}

	public static float remap(float value, float minIn, float maxIn, float minOut, float maxOut) {
		return Mth.lerp(Mth.inverseLerp(value, minIn, maxIn), minOut, maxOut);
	}
}
