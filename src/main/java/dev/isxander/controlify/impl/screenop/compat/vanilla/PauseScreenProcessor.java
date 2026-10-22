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
import dev.isxander.controlify.mixins.feature.screenop.impl.outofgame.PauseScreenAccessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PauseScreenProcessor extends ScreenProcessor<PauseScreen> {
	private final Supplier<@Nullable Button> disconnectButtonSupplier;

	public PauseScreenProcessor(PauseScreen screen, Supplier<@Nullable Button> disconnectButtonSupplier) {
		super(screen);
		this.disconnectButtonSupplier = disconnectButtonSupplier;
	}

	@Override
	protected void handleButtons(ControllerImpl controller) {
		super.handleButtons(controller);

		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumePulse()) {
			MinecraftUtil.setScreen(new OptionsScreen(screen, minecraft.options /*? if <26.3 >>*//*,true*/ ));
		}
		if (ControlifyActions.GUI_ABSTRACT_ACTION_2.on(controller).pulse().consumePulse()) {
			screen.setFocused(disconnectButtonSupplier.get());
		}
	}

	@Override
	public void onWidgetRebuild() {
		super.onWidgetRebuild();

		if (((PauseScreenAccessor) screen).getShowPauseMenu()) {
			getWidget("menu.returnToGame").ifPresent(widget -> {
				ButtonGuideApi.addGuideToButton(
						(AbstractButton) widget,
						ControlifyActions.GUI_BACK,
						ButtonGuidePredicate.always()
				);
			});
			getWidget("menu.options").ifPresent( widget -> {
				ButtonGuideApi.addGuideToButton(
						(AbstractButton) widget,
						ControlifyActions.GUI_ABSTRACT_ACTION_1,
						ButtonGuidePredicate.always()
				);
			});

			Button disconnectButton = disconnectButtonSupplier.get();
			if (disconnectButton != null) {
				ButtonGuideApi.addGuideToButton(
						disconnectButton,
						() -> disconnectButton.isFocused()
								? ControlifyActions.GUI_PRESS
								: ControlifyActions.GUI_ABSTRACT_ACTION_2,
						ButtonGuidePredicate.always()
				);
			}
		}
	}
}
