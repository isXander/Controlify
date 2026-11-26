/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.virtualmouse;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import dev.isxander.controlify.impl.screenop.VirtualMouseState;
import dev.isxander.controlify.impl.utils.ControllerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenDirection;
import org.joml.Vector2d;

public final class CursorMovementBehaviour implements ScreenBehaviour {
	private final ScreenContextImpl<?> context;
	private final VirtualMouseState mouse;

	public CursorMovementBehaviour(ScreenContextImpl<?> context) {
		this.context = context;
		this.mouse = context.virtualMouse();
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		Vector2d impulse = ControllerUtils.applyEasingToLength(
				ControlifyActions.VMOUSE_MOVE_RIGHT.on(controller).continuous().getContinuous() - ControlifyActions.VMOUSE_MOVE_LEFT.on(controller).continuous().getContinuous(),
				ControlifyActions.VMOUSE_MOVE_DOWN.on(controller).continuous().getContinuous() - ControlifyActions.VMOUSE_MOVE_UP.on(controller).continuous().getContinuous(),
				x -> Math.pow(x, 3));
		Vector2d previous = mouse.previousMoveImpulse();
		mouse.setMoveImpulse(impulse);
		mouse.refreshSnapPoints();
		if (impulse.x == 0 && impulse.y == 0 && (previous.x != 0 || previous.y != 0)) {
			mouse.snapToClosestPoint();
		}
		var settings = ((ControllerImpl) controller).input().settings().sensitivity;
		var window = Minecraft.getInstance().getWindow();
		double scale = settings.isLCE ? 10.0 * window.getScreenWidth() / window.getGuiScaledWidth()
				: 20.0 * Math.max(window.getWidth(), window.getHeight()) / 800.0;
		mouse.move(impulse.x * scale * settings.virtualMouseSensitivity, impulse.y * scale * settings.virtualMouseSensitivity);
		for (ScreenDirection direction : ScreenDirection.values()) {
			var action = switch (direction) {
				case UP -> ControlifyActions.VMOUSE_SNAP_UP;
				case DOWN -> ControlifyActions.VMOUSE_SNAP_DOWN;
				case LEFT -> ControlifyActions.VMOUSE_SNAP_LEFT;
				case RIGHT -> ControlifyActions.VMOUSE_SNAP_RIGHT;
			};
			while (context.isCurrentScreen() && action.on(controller).pulse().consumePulse()) {
				mouse.snapInDirection(direction);
			}
		}
		next.run();
	}
}
