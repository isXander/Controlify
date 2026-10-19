/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.api.gui.ButtonGuideApi;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.impl.bindings.ControlifyBindings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.TitleScreen;

public class TitleScreenProcessor extends ScreenProcessor<TitleScreen> {
	public TitleScreenProcessor(TitleScreen screen) {
		super(screen);
	}

	@Override
	protected void handleButtons(ControllerImpl controller) {
		if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
			getWidget("menu.quit").ifPresent(widget -> {
				screen.setFocused(widget);
				playClackSound();
			});
		}

		super.handleButtons(controller);

		if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
			if (getWidget("menu.options").isPresent()) {
				MinecraftUtil.setScreen(new OptionsScreen(screen, minecraft.options /*? if <26.3 >>*//*,false*/ ));
				playClackSound();
			}
		}
	}

	@Override
	public void onWidgetRebuild() {
		super.onWidgetRebuild();

		getWidget("menu.quit").ifPresent(widget -> {
			var button = (AbstractButton) widget;
			ButtonGuideApi.addGuideToButton(
					button,
					() -> button.isFocused() ? ControlifyBindings.GUI_PRESS : ControlifyBindings.GUI_BACK,
					ButtonGuidePredicate.always()
			);
		});

		getWidget("menu.options").ifPresent(widget -> {
			var button = (AbstractButton) widget;
			ButtonGuideApi.addGuideToButton(
					button,
					ControlifyBindings.GUI_ABSTRACT_ACTION_1,
					ButtonGuidePredicate.always()
			);
		});

	}
}
