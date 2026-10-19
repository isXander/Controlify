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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.function.Supplier;

public class JoinMultiplayerScreenProcessor extends ScreenProcessor<JoinMultiplayerScreen> {
	private final Supplier<ServerSelectionList> listSupplier;
	private final Supplier<Button> backButtonSupplier;
	private final Supplier<Button> directConnectButtonSupplier;
	private final Supplier<Button> addServerButtonSupplier;

	public JoinMultiplayerScreenProcessor(
			JoinMultiplayerScreen screen,
			Supplier<ServerSelectionList> listSupplier,
			Supplier<Button> backButtonSupplier,
			Supplier<Button> directConnectButtonSupplier,
			Supplier<Button> addServerButtonSupplier
	) {
		super(screen);
		this.listSupplier = listSupplier;
		this.backButtonSupplier = backButtonSupplier;
		this.directConnectButtonSupplier = directConnectButtonSupplier;
		this.addServerButtonSupplier = addServerButtonSupplier;
	}

	@Override
	protected void handleButtons(ControllerImpl controller) {
		if (screen.getFocused() != null && screen.getFocused() instanceof Button) {
			if (ControlifyBindings.GUI_BACK.on(controller).guiPressed().get()) {
				screen.setFocused(listSupplier.get());
				return;
			}
		}

		if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).guiPressed().get()) {
			Button directConnectButton = this.directConnectButtonSupplier.get();
			if (directConnectButton != null) {
				playClackSound();
				directConnectButton.onPress(null);
			}
		}

		if (ControlifyBindings.GUI_ABSTRACT_ACTION_2.on(controller).guiPressed().get()) {
			Button addServerButton = this.addServerButtonSupplier.get();
			if (addServerButton != null) {
				playClackSound();
				addServerButton.onPress(null);
			}
		}

		super.handleButtons(controller);
	}

	@Override
	public void onWidgetRebuild() {
		Button backButton = backButtonSupplier.get();
		Button directConnectButton = directConnectButtonSupplier.get();
		Button addServerButton = addServerButtonSupplier.get();

		if (backButton != null) {
			ButtonGuideApi.addGuideToButton(
					backButton,
					ControlifyBindings.GUI_BACK,
					ButtonGuidePredicate.always()
			);
		}

		if (directConnectButton != null) {
			ButtonGuideApi.addGuideToButton(
					directConnectButton,
					ControlifyBindings.GUI_ABSTRACT_ACTION_1,
					ButtonGuidePredicate.always()
			);
		}

		if (addServerButton != null) {
			ButtonGuideApi.addGuideToButton(
					addServerButton,
					ControlifyBindings.GUI_ABSTRACT_ACTION_2,
					ButtonGuidePredicate.always()
			);
		}

		super.onWidgetRebuild();
	}
}
