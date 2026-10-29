/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import dev.isxander.controlify.impl.screenop.ScreenBehaviourRegistryImpl;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	// allow controller input to set initial focus
	@ModifyExpressionValue(
			method = "setInitialFocus()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/InputType;isKeyboard()Z"
			)
	)
	private boolean shouldSetInitialFocus(boolean isKeyboard) {
		if (ScreenBehaviourRegistryImpl.INSTANCE.isReady()
				&& ScreenContextImpl.get((Screen) (Object) this).virtualMouse().isRequired()) {
			return false;
		}
		return isKeyboard || Controlify.instance().currentInputMode().isController();
	}

	@Inject(method = "setInitialFocus()V", at = @At("HEAD"), cancellable = true)
	private void preserveCustomInitialFocus(CallbackInfo ci) {
		if (Controlify.instance().currentInputMode().isController()
				&& ((Screen) (Object) this).getFocused() != null) {
			ci.cancel();
		}
	}
}
