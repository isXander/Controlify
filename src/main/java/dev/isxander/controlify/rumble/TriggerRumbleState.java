/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.rumble;

public record TriggerRumbleState(float left, float right) {
	public static final TriggerRumbleState NONE = new TriggerRumbleState(0.0F, 0.0F);

	public boolean isZero() {
		return left == 0.0F && right == 0.0F;
	}

	public TriggerRumbleState mul(float multiplier) {
		return new TriggerRumbleState(left * multiplier, right * multiplier);
	}
}
