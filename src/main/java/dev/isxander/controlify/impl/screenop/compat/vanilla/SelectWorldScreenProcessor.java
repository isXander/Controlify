/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.impl.bindings.ControlifyBindings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.SelectWorldScreenAccessor;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
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
		if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
			playClackSound();
			var minecraft = Minecraft.getInstance();
			CreateWorldScreen.openFresh(minecraft, () -> MinecraftUtil.setScreen(screen));
			return;
		}

		if (screen.getFocused() != null && screen.getFocused() instanceof Button) {
			if (ControlifyBindings.GUI_BACK.on(controller).guiPressed().get()) {
				screen.setFocused(((SelectWorldScreenAccessor) screen).controlify$getList());
				return;
			}
		}

		super.handleButtons(controller);
	}
}
