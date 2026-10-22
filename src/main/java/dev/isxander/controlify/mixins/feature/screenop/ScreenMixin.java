/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.impl.Controlify;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	@Shadow
	public abstract void clearFocus();

	// allow controller input to set initial focus
	@ModifyExpressionValue(
			method = "setInitialFocus()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/InputType;isKeyboard()Z"
			)
	)
	private boolean shouldSetInitialFocus(boolean isKeyboard) {
		return isKeyboard || Controlify.instance().currentInputMode().isController();
	}

	// setInitialFocus just sends a tab focus event,
	// so if this is ever called once per screen open, it would have caused
	// tab to happen twice and the wrong widget to be focused
	@Inject(method = "setInitialFocus()V", at = @At("HEAD"))
	private void clearFocusBeforeTabbing(CallbackInfo ci) {
		this.clearFocus();
	}
}
