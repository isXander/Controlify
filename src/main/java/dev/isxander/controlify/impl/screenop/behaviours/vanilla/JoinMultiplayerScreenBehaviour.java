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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.jspecify.annotations.Nullable;

public final class JoinMultiplayerScreenBehaviour implements ScreenBehaviour {
	private final Access access;
	private final ScreenContext<? extends JoinMultiplayerScreen> context;
	private final JoinMultiplayerScreen screen;

	public JoinMultiplayerScreenBehaviour(ScreenContext<? extends JoinMultiplayerScreen> context) {
		this.context = context;
		this.screen = context.screen();
		this.access = (Access) context.screen();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (screen.getFocused() != null && screen.getFocused() instanceof Button) {
			if (ControlifyActions.GUI_BACK.on(controller).pulse().consumeAllPulses()) {
				screen.setFocused(access.controlify$getServerList());
				return;
			}
		}

		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			Button directConnectButton = access.controlify$getDirectConnectButton();
			if (directConnectButton != null) {
				context.playClickSound();
				directConnectButton.onPress(new net.minecraft.client.input.KeyEvent(com.mojang.blaze3d.platform.InputConstants.KEY_RETURN, 0, 0));
				return;
			}
		}

		if (ControlifyActions.GUI_ABSTRACT_ACTION_2.on(controller).pulse().consumeAllPulses()) {
			Button addServerButton = access.controlify$getAddServerButton();
			if (addServerButton != null) {
				context.playClickSound();
				addServerButton.onPress(new net.minecraft.client.input.KeyEvent(com.mojang.blaze3d.platform.InputConstants.KEY_RETURN, 0, 0));
				return;
			}
		}
		next.run();
	}

	@Override
	public void onWidgetRebuild(Runnable next) {
		Button backButton = access.controlify$getBackButton();
		Button directConnectButton = access.controlify$getDirectConnectButton();
		Button addServerButton = access.controlify$getAddServerButton();

		if (backButton != null) {
			context.buttonGuide().addGuideToButton(
					backButton,
					ControlifyActions.GUI_BACK,
					ButtonGuidePredicate.always()
			);
		}

		if (directConnectButton != null) {
			context.buttonGuide().addGuideToButton(
					directConnectButton,
					ControlifyActions.GUI_ABSTRACT_ACTION_1,
					ButtonGuidePredicate.always()
			);
		}

		if (addServerButton != null) {
			context.buttonGuide().addGuideToButton(
					addServerButton,
					ControlifyActions.GUI_ABSTRACT_ACTION_2,
					ButtonGuidePredicate.always()
			);
		}
		next.run();
	}

	public interface Access {
		ServerSelectionList controlify$getServerList();

		@Nullable Button controlify$getBackButton();

		@Nullable Button controlify$getDirectConnectButton();

		@Nullable Button controlify$getAddServerButton();
	}
}
