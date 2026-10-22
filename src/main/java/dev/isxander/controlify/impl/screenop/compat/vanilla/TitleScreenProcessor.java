/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.api.gui.ButtonGuideApi;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;

public class TitleScreenProcessor extends ScreenProcessor<TitleScreen> {
	public TitleScreenProcessor(TitleScreen screen) {
		super(screen);
	}

	@Override
	protected void handleButtons(ControllerImpl controller) {
		if (ControlifyActions.GUI_BACK.on(controller).pulse().consumePulse()) {
			getWidget("menu.quit").ifPresent(widget -> {
				screen.setFocused(widget);
				playClackSound();
			});
		}

		super.handleButtons(controller);

		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumePulse()) {
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
					() -> button.isFocused() ? ControlifyActions.GUI_PRESS : ControlifyActions.GUI_BACK,
					ButtonGuidePredicate.always()
			);
		});

		getWidget("menu.options").ifPresent(widget -> {
			var button = (AbstractButton) widget;
			ButtonGuideApi.addGuideToButton(
					button,
					ControlifyActions.GUI_ABSTRACT_ACTION_1,
					ButtonGuidePredicate.always()
			);
		});

	}
}
