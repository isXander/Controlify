/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat.vanilla;

import dev.isxander.controlify.impl.bindings.ControlifyBindings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.haptic.HapticEffects;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.controlify.impl.screenop.ComponentProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;

public class AbstractButtonComponentProcessor implements ComponentProcessor {
	private final AbstractButton button;

	public AbstractButtonComponentProcessor(AbstractButton button) {
		this.button = button;
	}

	@Override
	public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerImpl controller) {
		if (ControlifyBindings.GUI_PRESS.on(controller).guiPressed().get()) {
			controller.hdHaptics().ifPresent(hh -> hh.playHaptic(HapticEffects.NAVIGATE));
			button.playDownSound(Minecraft.getInstance().getSoundManager());
			button.onPress(new KeyEvent(InputConstants.KEY_RETURN, 0, 0));
			return true;
		}

		return false;
	}
}
