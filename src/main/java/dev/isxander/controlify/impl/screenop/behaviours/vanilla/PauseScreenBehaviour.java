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
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.PauseScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.jetbrains.annotations.Nullable;

public final class PauseScreenBehaviour implements ScreenBehaviour {
	private final Access access;
	private final ScreenContext<? extends PauseScreen> context;
	private final PauseScreen screen;
	private final Minecraft minecraft = Minecraft.getInstance();

	public PauseScreenBehaviour(ScreenContext<? extends PauseScreen> context) {
		this.context = context;
		this.screen = context.screen();
		this.access = (Access) context.screen();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {

		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			MinecraftUtil.setScreen(new OptionsScreen(screen, minecraft.options/*? if <26.3 {*//*, true*//*?}*/));
			return;
		}
		if (ControlifyActions.GUI_ABSTRACT_ACTION_2.on(controller).pulse().consumeAllPulses()) {
			ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses();
			screen.setFocused(access.controlify$getDisconnectButton());
		}
		next.run();
	}

	@Override
	public void onWidgetRebuild(Runnable next) {

		if (((PauseScreenAccessor) screen).getShowPauseMenu()) {
			ScreenWidgets.find(screen, "menu.returnToGame").ifPresent(widget -> {
				context.buttonGuide().addGuideToButton(
						(AbstractButton) widget,
						ControlifyActions.GUI_BACK,
						ButtonGuidePredicate.always()
				);
			});
			ScreenWidgets.find(screen, "menu.options").ifPresent(widget -> {
				context.buttonGuide().addGuideToButton(
						(AbstractButton) widget,
						ControlifyActions.GUI_ABSTRACT_ACTION_1,
						ButtonGuidePredicate.always()
				);
			});

			Button disconnectButton = access.controlify$getDisconnectButton();
			if (disconnectButton != null) {
				context.buttonGuide().addGuideToButton(
						disconnectButton,
						() -> disconnectButton.isFocused()
								? ControlifyActions.GUI_PRESS
								: ControlifyActions.GUI_ABSTRACT_ACTION_2,
						ButtonGuidePredicate.always()
				);
			}
		}
		next.run();
	}

	public interface Access {
		@Nullable Button controlify$getDisconnectButton();
	}
}
