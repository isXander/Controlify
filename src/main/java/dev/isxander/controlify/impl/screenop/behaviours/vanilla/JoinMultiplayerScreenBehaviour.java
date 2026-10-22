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

import java.util.function.Supplier;

public final class JoinMultiplayerScreenBehaviour implements ScreenBehaviour {
	private final ScreenContext<? extends JoinMultiplayerScreen> context;
	private final JoinMultiplayerScreen screen;

	private final Supplier<ServerSelectionList> listSupplier;
	private final Supplier<Button> backButtonSupplier;
	private final Supplier<Button> directConnectButtonSupplier;
	private final Supplier<Button> addServerButtonSupplier;

	public JoinMultiplayerScreenBehaviour(
			ScreenContext<? extends JoinMultiplayerScreen> context,
			Supplier<ServerSelectionList> listSupplier,
			Supplier<Button> backButtonSupplier,
			Supplier<Button> directConnectButtonSupplier,
			Supplier<Button> addServerButtonSupplier
	) {
		this.context = context;
		this.screen = context.screen();
		this.listSupplier = listSupplier;
		this.backButtonSupplier = backButtonSupplier;
		this.directConnectButtonSupplier = directConnectButtonSupplier;
		this.addServerButtonSupplier = addServerButtonSupplier;
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (screen.getFocused() != null && screen.getFocused() instanceof Button) {
			if (ControlifyActions.GUI_BACK.on(controller).pulse().consumeAllPulses()) {
				screen.setFocused(listSupplier.get());
				return;
			}
		}

		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			Button directConnectButton = this.directConnectButtonSupplier.get();
			if (directConnectButton != null) {
				context.playClickSound();
				directConnectButton.onPress(new net.minecraft.client.input.KeyEvent(com.mojang.blaze3d.platform.InputConstants.KEY_RETURN, 0, 0));
				return;
			}
		}

		if (ControlifyActions.GUI_ABSTRACT_ACTION_2.on(controller).pulse().consumeAllPulses()) {
			Button addServerButton = this.addServerButtonSupplier.get();
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
		Button backButton = backButtonSupplier.get();
		Button directConnectButton = directConnectButtonSupplier.get();
		Button addServerButton = addServerButtonSupplier.get();

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
}
