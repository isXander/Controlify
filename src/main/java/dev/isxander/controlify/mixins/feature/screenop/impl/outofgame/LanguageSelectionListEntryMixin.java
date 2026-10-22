/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.outofgame;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.screens.options.LanguageSelectScreen$LanguageSelectionList$Entry")
public class LanguageSelectionListEntryMixin implements ElementBehaviourFactory<GuiEventListener> {
	@Shadow
	@Final
	String code;

	@Override
	public ElementBehaviour create(GuiEventListener element) {
		return new ElementBehaviour() {
			@Override
			public void handleInteraction(ScreenContext<?> screen, Controller controller, Runnable next) {
				if (!ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses()) {
					next.run();
					return;
				}
				var minecraft = Minecraft.getInstance();
				var languages = minecraft.getLanguageManager();
				if (!code.equals(languages.getSelected())) {
					languages.setSelected(code);
					minecraft.options.languageCode = code;
					minecraft.reloadResourcePacks();
					minecraft.options.save();
				}
				screen.playClickSound();
				MinecraftUtil.setScreen(((OptionsSubScreenAccessor) screen.screen()).controlify$getLastScreen());
			}
		};
	}
}
