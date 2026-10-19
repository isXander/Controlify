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
import dev.isxander.controlify.impl.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.impl.screenop.keyboard.KeyboardLayouts;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;

public class AddServerLikeScreenProcessor extends ScreenProcessor<Screen> {

	private final Supplier<EditBox> ipEditBoxSupplier;
	private final Supplier<Button> doneButtonSupplier;
	private final Supplier<Button> backButtonSupplier;

	public AddServerLikeScreenProcessor(
			Screen screen,
			Supplier<EditBox> ipEditBoxSupplier,
			Supplier<Button> doneButtonSupplier,
			Supplier<Button> backButtonSupplier
	) {
		super(screen);
		this.ipEditBoxSupplier = ipEditBoxSupplier;
		this.doneButtonSupplier = doneButtonSupplier;
		this.backButtonSupplier = backButtonSupplier;
	}

	@Override
	public void onWidgetRebuild() {
		super.onWidgetRebuild();

		EditBox ipEditBox = ipEditBoxSupplier.get();
		Button doneButton = doneButtonSupplier.get();
		Button backButton = backButtonSupplier.get();

		if (ipEditBox != null) {
			var processor = (EditBoxComponentProcessor) ComponentProcessorProvider.provide(ipEditBox);
			processor.setKeyboardLayout(KeyboardLayouts.serverIp());
		}

		if (doneButton != null) {
			ButtonGuideApi.addGuideToButton(
					doneButton,
					() -> doneButton.isFocused()
							? ControlifyBindings.GUI_PRESS
							: ControlifyBindings.GUI_ABSTRACT_ACTION_1,
					ButtonGuidePredicate.always()
			);
		}

		if (backButton != null) {
			ButtonGuideApi.addGuideToButton(
					backButton,
					() -> ControlifyBindings.GUI_BACK,
					ButtonGuidePredicate.always()
			);
		}
	}

	@Override
	protected void handleButtons(ControllerImpl controller) {
		if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).guiPressed().get()) {
			Button doneButton = doneButtonSupplier.get();
			if (doneButton != null && !doneButton.isFocused()) {
				playClackSound();
				screen.setFocused(doneButton);
			}
		}

		super.handleButtons(controller);
	}
}
