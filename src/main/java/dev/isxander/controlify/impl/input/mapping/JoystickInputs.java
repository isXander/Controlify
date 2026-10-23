/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.input.mapping;

import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public final class JoystickInputs {
	private static final Identifier[] BUTTONS = new Identifier[256];
	private static final Identifier[] AXES = new Identifier[256 << 1];
	private static final Identifier[] HATS = new Identifier[128 << 2];

	public enum HatDirection {
		UP, RIGHT, DOWN, LEFT
	}

	private JoystickInputs() {
	}

	public static Identifier button(int index) {
		Identifier cache = BUTTONS[index];

		if (cache == null) {
			BUTTONS[index] = cache = CUtil.rl("button/" + index);
		}

		return cache;
	}

	public static Identifier axis(int index, boolean positive) {
		int cacheIndex = (index << 1) | (positive ? 0 : 1);

		Identifier cache = AXES[cacheIndex];

		if (cache == null) {
			AXES[cacheIndex] = cache = CUtil.rl("axis/" + index + "/" + (positive ? "positive" : "negative"));
		}

		return cache;
	}

	public static Identifier hat(int index, HatDirection direction) {
		int cacheIndex = (index << 2) | direction.ordinal();

		Identifier cache = HATS[cacheIndex];

		if (cache == null) {
			HATS[cacheIndex] = cache = CUtil.rl("hat/" + index + "/" + direction.name().toLowerCase());
		}

		return cache;
	}
}
