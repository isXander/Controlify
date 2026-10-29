/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.keyboard;

import dev.isxander.controlify.api.gui.font.ActionGlyphs;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.utils.LazyComponentDims;
import net.minecraft.network.chat.Component;

public final class CommonKeyboardHints {

	public static final LazyComponentDims TEXT_CURSOR = new LazyComponentDims(
			Component.translatable("controlify.hint.keyboard_cursor_movement",
					ActionGlyphs.action(ControlifyActions.GUI_PREV_TAB.actionId()),
					ActionGlyphs.action(ControlifyActions.GUI_NEXT_TAB.actionId()))
	);

	public static final LazyComponentDims OPEN_KEYBOARD = new LazyComponentDims(
			Component.translatable("controlify.hint.edit_box_keyboard",
					ActionGlyphs.action(ControlifyActions.GUI_PRESS.actionId()))
	);

	private CommonKeyboardHints() {
	}
}
