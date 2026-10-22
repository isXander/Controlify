/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.sodium.screenop;

//? if sodium {

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.compatibility.sodium.mixins.FlatButtonWidgetAccessor;

public final class SodiumScreenBehaviour implements ScreenBehaviour {
	private final SodiumScreenOperations operations;

	private final ScreenContext<?> context;

	public SodiumScreenBehaviour(ScreenContext<?> context, SodiumScreenOperations operations) {
		this.context = context;
		this.operations = operations;
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (ControlifyActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			((FlatButtonWidgetAccessor) operations.controlify$getApplyButton()).invokeDoAction();
		}
		if (ControlifyActions.GUI_ABSTRACT_ACTION_2.on(controller).pulse().consumeAllPulses()) {
			((FlatButtonWidgetAccessor) operations.controlify$getUndoButton()).invokeDoAction();
		}

		next.run();
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		while (ControlifyActions.GUI_NEXT_TAB.on(controller).pulse().consumePulse()) {
			operations.controlify$nextPage();
		}

		while (ControlifyActions.GUI_PREV_TAB.on(controller).pulse().consumePulse()) {
			operations.controlify$prevPage();
		}

		next.run();
	}

	@Override
	public void onWidgetRebuild(Runnable next) {
		context.buttonGuide().addGuideToButton(
				operations.controlify$getApplyButton(),
				ControlifyActions.GUI_ABSTRACT_ACTION_1,
				ButtonGuidePredicate.always()
		);
		context.buttonGuide().addGuideToButton(
				operations.controlify$getUndoButton(),
				ControlifyActions.GUI_ABSTRACT_ACTION_2,
				ButtonGuidePredicate.always()
		);

		context.buttonGuide().addGuideToButton(
				operations.controlify$getCloseButton(),
				ControlifyActions.GUI_BACK,
				ButtonGuidePredicate.always()
		);

		next.run();
	}
}
//?}
