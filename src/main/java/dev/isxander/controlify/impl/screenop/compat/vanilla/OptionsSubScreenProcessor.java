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
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;

import java.util.function.Supplier;

public class OptionsSubScreenProcessor<T extends OptionsSubScreen> extends ScreenProcessor<T> {
	private final Supplier<Button> doneButtonSupplier;

	public OptionsSubScreenProcessor(T screen, Supplier<Button> doneButtonSupplier) {
		super(screen);
		this.doneButtonSupplier = doneButtonSupplier;
	}

	@Override
	public void onWidgetRebuild() {
		super.onWidgetRebuild();

		Button doneButton = doneButtonSupplier.get();
		if (doneButton != null) {
			ButtonGuideApi.addGuideToButton(
					doneButton,
					ControlifyActions.GUI_BACK,
					ButtonGuidePredicate.always()
			);
		}
	}
}
