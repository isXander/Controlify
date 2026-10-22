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
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.OptionsSubScreenAccessor;
import net.minecraft.client.Minecraft;

public class LanguageSelectionListComponentProcessor implements ComponentProcessor {
	private final String code;

	public LanguageSelectionListComponentProcessor(String code) {
		this.code = code;
	}

	@Override
	public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerImpl controller) {
		if (ControlifyActions.GUI_PRESS.on(controller).pulse().consumePulse()) {
			var minecraft = Minecraft.getInstance();
			var languageManager = minecraft.getLanguageManager();
			if (!code.equals(languageManager.getSelected())) {
				languageManager.setSelected(code);
				minecraft.options.languageCode = code;
				minecraft.reloadResourcePacks();
				minecraft.options.save();
			}

			MinecraftUtil.setScreen(((OptionsSubScreenAccessor) screen.screen).controlify$getLastScreen());

			return true;
		}

		return false;
	}
}
