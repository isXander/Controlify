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
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public final class ServerAddressBehaviour implements ScreenBehaviour {
	private final ScreenContext<? extends Screen> context;
	private final Screen screen;

	private final Supplier<@Nullable Button> doneButtonSupplier;
	private final Supplier<@Nullable Button> backButtonSupplier;

	public ServerAddressBehaviour(
			ScreenContext<? extends Screen> context,
			Supplier<@Nullable Button> doneButtonSupplier,
			Supplier<@Nullable Button> backButtonSupplier
	) {
		this.context = context;
		this.screen = context.screen();
		this.doneButtonSupplier = doneButtonSupplier;
		this.backButtonSupplier = backButtonSupplier;
	}

	@Override
	public void onWidgetRebuild(Runnable next) {

		Button doneButton = doneButtonSupplier.get();
		Button backButton = backButtonSupplier.get();

		if (doneButton != null) {
			context.buttonGuide().addGuideToButton(
					doneButton,
					() -> doneButton.isFocused()
							? ControlifyActions.GUI_PRESS
							: ControlifyActions.GUI_ABSTRACT_ACTION_1,
					ButtonGuidePredicate.always()
			);
		}

		if (backButton != null) {
			context.buttonGuide().addGuideToButton(
					backButton,
					() -> ControlifyActions.GUI_BACK,
					ButtonGuidePredicate.always()
			);
		}
		next.run();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			Button doneButton = doneButtonSupplier.get();
			if (doneButton != null && !doneButton.isFocused()) {
				ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses();
				context.playClickSound();
				screen.setFocused(doneButton);
			}
		}
		next.run();
	}
}
