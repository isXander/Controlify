/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ComponentProcessor;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.SelectWorldScreenAccessor;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

public class WorldListEntryComponentProcessor implements ComponentProcessor {
	@Override
	public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerImpl controller) {
		if (ControlifyActions.GUI_PRESS.on(controller).pulse().consumePulse()) {
			var selectWorldScreen = (SelectWorldScreen) screen.screen;
			selectWorldScreen.setFocused(((SelectWorldScreenAccessor) selectWorldScreen).controlify$getPlayWorldButton());

			return true;
		}

		return false;
	}
}
