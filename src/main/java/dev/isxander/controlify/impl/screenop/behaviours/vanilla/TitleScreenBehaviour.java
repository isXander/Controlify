/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.screenop.ScreenWidgets;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;

public final class TitleScreenBehaviour implements ScreenBehaviour {
	private final ScreenContext<? extends TitleScreen> context;
	private final TitleScreen screen;
	private final Minecraft minecraft = Minecraft.getInstance();

	public TitleScreenBehaviour(ScreenContext<? extends TitleScreen> context) {
		this.context = context;
		this.screen = context.screen();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (ControlifyActions.GUI_BACK.on(controller).pulse().consumeAllPulses()) {
			ScreenWidgets.find(screen, "menu.quit").ifPresent(widget -> {
				ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses();
				screen.setFocused(widget);
				context.playClickSound();
			});
		}

		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			if (ScreenWidgets.find(screen, "menu.options").isPresent()) {
				MinecraftUtil.setScreen(new OptionsScreen(screen, minecraft.options /*? if <26.3 >>*//*,false*/));
				context.playClickSound();
			}
		}
		next.run();
	}

	@Override
	public void onWidgetRebuild(Runnable next) {

		ScreenWidgets.find(screen, "menu.quit").ifPresent(widget -> {
			var button = (AbstractButton) widget;
			context.buttonGuide().addGuideToButton(
					button,
					() -> button.isFocused() ? ControlifyActions.GUI_PRESS : ControlifyActions.GUI_BACK,
					ButtonGuidePredicate.always()
			);
		});

		ScreenWidgets.find(screen, "menu.options").ifPresent(widget -> {
			var button = (AbstractButton) widget;
			context.buttonGuide().addGuideToButton(
					button,
					ControlifyActions.GUI_ABSTRACT_ACTION_1,
					ButtonGuidePredicate.always()
			);
		});
		next.run();
	}
}
