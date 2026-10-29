/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.virtualmouse;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InputConstants.class)
public class InputConstantsMixin {
	// must modify isKeyDown here because Screen.hasShiftDown has some instances that ask for this directly.
	@ModifyReturnValue(method = "isKeyDown", at = @At("RETURN"))
	private static boolean modifyIsKeyDown(boolean keyDown, @Local(argsOnly = true, name = "key") int key) {
		if (key == InputConstants.KEY_LSHIFT) {
			ControllerImpl controller = Controlify.instance().getCurrentController().orElse(null);
			if (controller == null) {
				return keyDown;
			}

			return keyDown
					|| ControlifyActions.VMOUSE_SHIFT_CLICK.on(controller).latch().isLatchActive()
					|| ControlifyActions.VMOUSE_SHIFT.on(controller).latch().isLatchActive();
		}

		return keyDown;
	}
}
