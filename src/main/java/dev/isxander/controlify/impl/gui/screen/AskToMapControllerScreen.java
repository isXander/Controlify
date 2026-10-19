/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.gui.screen;

import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AskToMapControllerScreen extends ConfirmScreen {
	public AskToMapControllerScreen(ControllerImpl controller, Screen lastScreen) {
		super(
				(confirmed) -> {
					if (confirmed) {
						MinecraftUtil.setScreen(ControllerMappingMakerScreen.createGamepadMapping(controller.input().orElseThrow(), lastScreen));
					} else {
						MinecraftUtil.setScreen(lastScreen);
					}
				},
				Component.translatable("controlify.ask_to_map_controller.title"),
				Component.translatable("controlify.ask_to_map_controller.message"),
				Component.translatable("controlify.ask_to_map_controller.yes"),
				Component.translatable("controlify.ask_to_map_controller.no")
		);
	}
}
