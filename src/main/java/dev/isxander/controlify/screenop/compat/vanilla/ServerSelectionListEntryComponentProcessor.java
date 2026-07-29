/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.JoinMultiplayerScreenAccessor;

public class ServerSelectionListEntryComponentProcessor implements ComponentProcessor {
	@Override
	public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerEntity controller) {
		if (ControlifyBindings.GUI_PRESS.on(controller).guiPressed().get()) {
			screen.screen.setFocused(((JoinMultiplayerScreenAccessor) screen.screen).controlify$getJoinButton());
			return true;
		}

		return false;
	}
}
