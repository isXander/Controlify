/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.SelectWorldScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

public class SelectWorldScreenProcessor extends ScreenProcessor<SelectWorldScreen> {
	public SelectWorldScreenProcessor(SelectWorldScreen screen) {
		super(screen);
	}

	@Override
	protected void handleButtons(ControllerImpl controller) {
		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumePulse()) {
			playClackSound();
			var minecraft = Minecraft.getInstance();
			CreateWorldScreen.openFresh(minecraft, () -> MinecraftUtil.setScreen(screen));
			return;
		}

		if (screen.getFocused() != null && screen.getFocused() instanceof Button) {
			if (ControlifyActions.GUI_BACK.on(controller).pulse().consumePulse()) {
				screen.setFocused(((SelectWorldScreenAccessor) screen).controlify$getList());
				return;
			}
		}

		super.handleButtons(controller);
	}
}
