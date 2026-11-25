/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.utils;

import com.mojang.blaze3d.platform.InputConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyEventsTest {
	/// Edit boxes switch on the shortcut key, so a synthesised backspace must report backspace as such.
	@Test
	void synthesisedBackspaceReportsBackspace() {
		var event = KeyEvents.of(InputConstants.KEY_BACKSPACE, 0, 0);
		assertEquals(InputConstants.KEY_BACKSPACE, event.key());
		assertEquals(InputConstants.KEY_BACKSPACE, event.shortcutKey());
	}
}
