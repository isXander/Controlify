/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework.controller;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public interface TestControllerStateView<E> {
	int getPlayerIndex();

	short getLowFrequencyRumble();
	short getHighFrequencyRumble();

	short getLeftTriggerRumble();
	short getRightTriggerRumble();

	byte getLedRed();
	byte getLedGreen();
	byte getLedBlue();

	@Nullable ByteBuffer getLatestEffect();
	@Nullable E getGamepadEffectState();

	boolean getSensorsEnabled();

	default boolean hasRumble() {
		return getLowFrequencyRumble() != 0 || getHighFrequencyRumble() != 0;
	}
}
