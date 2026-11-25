/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.font;

import dev.isxander.controlify.impl.input.mapping.JoystickInputs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputFontMapperTest {
	@Test
	void unmappedJoystickInputsGetTextLabels() {
		assertEquals("B3", InputFontMapper.fallbackLabel(JoystickInputs.button(3)));
		assertEquals("A1+", InputFontMapper.fallbackLabel(JoystickInputs.axis(1, true)));
		assertEquals("A1-", InputFontMapper.fallbackLabel(JoystickInputs.axis(1, false)));
		assertEquals("H0↑", InputFontMapper.fallbackLabel(JoystickInputs.hat(0, JoystickInputs.HatDirection.UP)));
	}
}
