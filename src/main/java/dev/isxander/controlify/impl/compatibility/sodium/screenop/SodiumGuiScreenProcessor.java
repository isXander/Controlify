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
import dev.isxander.controlify.impl.bindings.ControlifyBindings;
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
		if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) {
			((FlatButtonWidgetAccessor) operations.controlify$getApplyButton()).invokeDoAction();
		}
		if (ControlifyBindings.GUI_ABSTRACT_ACTION_2.on(controller).justPressed()) {
			((FlatButtonWidgetAccessor) operations.controlify$getUndoButton()).invokeDoAction();
		}

		if (ControlifyBindings.GUI_NEXT_TAB.on(controller).justPressed()) {
			operations.controlify$nextPage();
		}

		if (ControlifyBindings.GUI_PREV_TAB.on(controller).justPressed()) {
			operations.controlify$prevPage();
		}

		super.handleButtons(controller);
	}

	public void onRebuildGUI() {
		ButtonGuideApi.addGuideToButton(
				operations.controlify$getApplyButton(),
				ControlifyBindings.GUI_ABSTRACT_ACTION_1,
				ButtonGuidePredicate.always()
		);
		ButtonGuideApi.addGuideToButton(
				operations.controlify$getUndoButton(),
				ControlifyBindings.GUI_ABSTRACT_ACTION_2,
				ButtonGuidePredicate.always()
		);

		ButtonGuideApi.addGuideToButton(
				operations.controlify$getCloseButton(),
				ControlifyBindings.GUI_BACK,
				ButtonGuidePredicate.always()
		);

		super.onWidgetRebuild();
	}
}
//?}
