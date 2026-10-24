/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.screen;

import dev.isxander.controlify.api.InputMode;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import org.jetbrains.annotations.Nullable;

public final class FocusManagementBehaviour implements ScreenBehaviour {
	private final ScreenContextImpl<?> context;
	private @Nullable InputMode previousMode;
	private boolean previousVirtualMouse;

	public FocusManagementBehaviour(ScreenContextImpl<?> context) {
		this.context = context;
	}

	@Override
	public void update(float delta, Runnable next) {
		synchronizeInputMode();
		next.run();
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		synchronizeInputMode();
		next.run();
	}

	@Override
	public void onScreenActiveChanged(boolean active) {
		if (active) {
			previousMode = null;
			synchronizeInputMode();
		}
	}

	@Override
	public void onWidgetRebuild(Runnable next) {
		if (context.inputMode().isController() && !context.isVirtualMouseEnabled()) {
			Minecraft.getInstance().setLastInputType(InputType.KEYBOARD_ARROW);
		}
		previousMode = null;
		synchronizeInputMode();
		next.run();
	}

	private void synchronizeInputMode() {
		if (!context.isCurrentScreen()) {
			return;
		}
		InputMode mode = context.inputMode();
		boolean mouse = context.isVirtualMouseEnabled();
		if (mode == previousMode && mouse == previousVirtualMouse) {
			return;
		}
		previousMode = mode;
		previousVirtualMouse = mouse;
		if (mouse || mode == InputMode.KEYBOARD_MOUSE) {
			if (mouse || context.focusPath().stream().noneMatch(EditBox.class::isInstance)) {
				context.clearFocus();
			}
		} else if (context.screen().getFocused() == null) {
			context.setInitialFocus();
		}
	}

}
