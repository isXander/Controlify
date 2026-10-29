/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.vanilla;

import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.impl.action.ControlifyActions;
import net.minecraft.client.gui.components.Button;
import org.jspecify.annotations.Nullable;

public final class DoneButtonGuideBehaviour implements ScreenBehaviour {
	private final Access access;
	private final ScreenContext<?> context;

	public DoneButtonGuideBehaviour(ScreenContext<?> context) {
		this.context = context;
		this.access = (Access) context.screen();
	}

	@Override
	public void onWidgetRebuild(Runnable next) {
		var done = access.controlify$getDoneButton();
		if (done != null) {
			context.buttonGuide().addGuideToButton(done, ControlifyActions.GUI_BACK, ButtonGuidePredicate.always());
		}
		next.run();
	}

	public interface Access {
		@Nullable Button controlify$getDoneButton();
	}
}
