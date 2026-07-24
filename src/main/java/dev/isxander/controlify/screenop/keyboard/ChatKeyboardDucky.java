/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.screenop.keyboard;

import net.minecraft.client.gui.screens.ChatScreen;

public interface ChatKeyboardDucky {
	float controlify$keyboardShiftAmount();

	static float getKeyboardShiftAmount(ChatScreen screen) {
		return ((ChatKeyboardDucky) screen).controlify$keyboardShiftAmount();
	}
}
