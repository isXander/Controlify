/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compat.sodium.screenop;

//? if sodium {

import dev.isxander.controlify.api.action.BuiltinActions;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.compat.sodium.mixins.FlatButtonWidgetAccessor;

public final class SodiumScreenBehaviour implements ScreenBehaviour {
	private final SodiumScreenOperations operations;

	private final ScreenContext<?> context;

	public SodiumScreenBehaviour(ScreenContext<?> context, SodiumScreenOperations operations) {
		this.context = context;
		this.operations = operations;
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (BuiltinActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
			((FlatButtonWidgetAccessor) operations.controlify$getApplyButton()).invokeDoAction();
		}
		if (BuiltinActions.GUI_ABSTRACT_ACTION_2.on(controller).pulse().consumeAllPulses()) {
			((FlatButtonWidgetAccessor) operations.controlify$getUndoButton()).invokeDoAction();
		}

		next.run();
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		while (BuiltinActions.GUI_NEXT_TAB.on(controller).pulse().consumePulse()) {
			operations.controlify$nextPage();
		}

		while (BuiltinActions.GUI_PREV_TAB.on(controller).pulse().consumePulse()) {
			operations.controlify$prevPage();
		}

		next.run();
	}

	@Override
	public void onWidgetRebuild(Runnable next) {
		context.buttonGuide().addGuideToButton(
				operations.controlify$getApplyButton(),
				BuiltinActions.GUI_ABSTRACT_ACTION_1,
				ButtonGuidePredicate.always()
		);
		context.buttonGuide().addGuideToButton(
				operations.controlify$getUndoButton(),
				BuiltinActions.GUI_ABSTRACT_ACTION_2,
				ButtonGuidePredicate.always()
		);

		context.buttonGuide().addGuideToButton(
				operations.controlify$getCloseButton(),
				BuiltinActions.GUI_BACK,
				ButtonGuidePredicate.always()
		);

		next.run();
	}
}
//?}
