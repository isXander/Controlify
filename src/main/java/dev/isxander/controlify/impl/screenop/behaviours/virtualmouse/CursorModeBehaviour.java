/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.virtualmouse;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import dev.isxander.controlify.impl.screenop.VirtualMouseState;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.network.chat.Component;

/// Keeps native cursor state in sync with activation, input mode and user toggles.
public final class CursorModeBehaviour implements ScreenBehaviour {
	private final ScreenContextImpl<?> context;
	private final VirtualMouseState mouse;

	public CursorModeBehaviour(ScreenContextImpl<?> context) {
		this.context = context;
		this.mouse = context.virtualMouse();
	}

	@Override
	public void onScreenActiveChanged(boolean active) {
		if (active) {
			mouse.activate();
		} else {
			mouse.deactivate();
		}
	}

	@Override
	public void update(float delta, Runnable next) {
		boolean wasEnabled = mouse.isEnabled();
		mouse.synchronizeMode();
		if (mouse.isEnabled()) {
			mouse.updatePosition(delta);
		}
		if (wasEnabled != mouse.isEnabled()) {
			Controlify.instance().hideMouse(context.inputMode().isController(), !mouse.isEnabled());
		}
		next.run();
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		mouse.releaseUnlatchedClicks(controller);
		next.run();
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		while (context.isCurrentScreen() && ControlifyActions.VMOUSE_TOGGLE.on(controller).pulse().consumePulse()) {
			if (mouse.mode() != VirtualMouseMode.DEFAULT) {
				MinecraftUtil.sendToast(Component.translatable("controlify.toast.vmouse_unavailable.title"),
						Component.translatable("controlify.toast.vmouse_unavailable.description"), false);
				continue;
			}
			var screens = Controlify.instance().config().getSettings().globalSettings().virtualMouseScreens;
			var type = context.screen().getClass();
			boolean enabled = !screens.remove(type);
			if (enabled) {
				screens.add(type);
			}
			mouse.synchronizeMode();
			Controlify.instance().hideMouse(true, !mouse.isEnabled());
			String toast = enabled ? "controlify.toast.vmouse_enabled" : "controlify.toast.vmouse_disabled";
			MinecraftUtil.sendToast(Component.translatable(toast + ".title"), Component.translatable(toast + ".description"), false);
			Controlify.instance().config().saveSafely();
		}
		next.run();
	}
}
