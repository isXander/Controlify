/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.sodium.screenop;

//? if sodium {

import dev.isxander.controlify.api.gui.ButtonGuideApi;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.compatibility.sodium.mixins.FlatButtonWidgetAccessor;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import net.minecraft.client.gui.screens.Screen;

public class SodiumGuiScreenProcessor extends ScreenProcessor<Screen> {
	private final SodiumScreenOperations operations;

	public SodiumGuiScreenProcessor(Screen screen, SodiumScreenOperations operations) {
		super(screen);
		this.operations = operations;
	}

	@Override
	protected void handleComponentNavigation(ControllerImpl controller) {
		super.handleComponentNavigation(controller);
	}

	@Override
	protected void handleButtons(ControllerImpl controller) {
		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumePulse()) {
			((FlatButtonWidgetAccessor) operations.controlify$getApplyButton()).invokeDoAction();
		}
		if (ControlifyActions.GUI_ABSTRACT_ACTION_2.on(controller).pulse().consumePulse()) {
			((FlatButtonWidgetAccessor) operations.controlify$getUndoButton()).invokeDoAction();
		}

		if (ControlifyActions.GUI_NEXT_TAB.on(controller).pulse().consumePulse()) {
			operations.controlify$nextPage();
		}

		if (ControlifyActions.GUI_PREV_TAB.on(controller).pulse().consumePulse()) {
			operations.controlify$prevPage();
		}

		super.handleButtons(controller);
	}

	public void onRebuildGUI() {
		ButtonGuideApi.addGuideToButton(
				operations.controlify$getApplyButton(),
				ControlifyActions.GUI_ABSTRACT_ACTION_1,
				ButtonGuidePredicate.always()
		);
		ButtonGuideApi.addGuideToButton(
				operations.controlify$getUndoButton(),
				ControlifyActions.GUI_ABSTRACT_ACTION_2,
				ButtonGuidePredicate.always()
		);

		ButtonGuideApi.addGuideToButton(
				operations.controlify$getCloseButton(),
				ControlifyActions.GUI_BACK,
				ButtonGuidePredicate.always()
		);

		super.onWidgetRebuild();
	}
}
//?}
