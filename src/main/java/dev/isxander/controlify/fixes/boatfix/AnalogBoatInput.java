/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.fixes.boatfix;

import dev.isxander.controlify.mixins.feature.patches.analogueboat.AbstractBoatMixin;

/**
 * @see AbstractBoatMixin
 */
public interface AnalogBoatInput {
	void controlify$setAnalogInput(float forward, float right);
}
