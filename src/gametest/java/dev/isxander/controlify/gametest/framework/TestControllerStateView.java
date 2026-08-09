/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework;

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

	ByteBuffer getLatestEffect();
	E getGamepadEffectState();

	boolean getSensorsEnabled();

	default boolean hasRumble() {
		return getLowFrequencyRumble() != 0 || getHighFrequencyRumble() != 0;
	}
}
