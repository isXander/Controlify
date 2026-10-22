/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.input.KeyEvent;

import java.util.List;

/// Navigation and activation through the focused element path.
public final class FocusInputBehaviour implements ScreenBehaviour {
	private final ScreenContextImpl<?> context;

	public FocusInputBehaviour(ScreenContextImpl<?> context) {
		this.context = context;
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		if (context.screen().getFocused() == null) {
			context.setInitialFocus();
		}
		for (ScreenDirection direction : List.of(ScreenDirection.RIGHT, ScreenDirection.LEFT, ScreenDirection.UP, ScreenDirection.DOWN)) {
			var action = switch (direction) {
				case RIGHT -> ControlifyActions.GUI_NAVI_RIGHT;
				case LEFT -> ControlifyActions.GUI_NAVI_LEFT;
				case UP -> ControlifyActions.GUI_NAVI_UP;
				case DOWN -> ControlifyActions.GUI_NAVI_DOWN;
			};
			while (context.isCurrentScreen() && action.on(controller).pulse().consumePulse()) {
				var path = context.screen().nextFocusPath(new FocusNavigationEvent.ArrowNavigation(direction));
				if (path != null) {
					Minecraft.getInstance().setLastInputType(InputType.KEYBOARD_ARROW);
					context.changeFocus(path);
					context.notifyNavigation(controller);
				}
			}
		}
		next.run();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses()) {
			var focused = context.focusPath();
			// A widget can be disabled or hidden while it still holds focus. Do not
			// reactivate it through the generic keyboard path after its behaviour skips.
			boolean unavailable = focused.stream().anyMatch(element ->
					element instanceof AbstractWidget widget && (!widget.active || !widget.visible));
			if (!unavailable && !context.tryOpenKeyboard(controller, focused.isEmpty() ? null : focused.getFirst())) {
				context.screen().keyPressed(new KeyEvent(InputConstants.KEY_RETURN, 0, 0));
			}
		}
		next.run();
	}
}
