/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.SelectWorldScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

public final class SelectWorldScreenBehaviour implements ScreenBehaviour {
	private final ScreenContext<? extends SelectWorldScreen> context;
	private final SelectWorldScreen screen;

	public SelectWorldScreenBehaviour(ScreenContext<? extends SelectWorldScreen> context) {
		this.context = context;
		this.screen = context.screen();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			context.playClickSound();
			var minecraft = Minecraft.getInstance();
			CreateWorldScreen.openFresh(minecraft, () -> MinecraftUtil.setScreen(screen));
			return;
		}

		if (screen.getFocused() != null && screen.getFocused() instanceof Button) {
			if (ControlifyActions.GUI_BACK.on(controller).pulse().consumeAllPulses()) {
				screen.setFocused(((SelectWorldScreenAccessor) screen).controlify$getList());
				return;
			}
		}
		next.run();
	}
}
