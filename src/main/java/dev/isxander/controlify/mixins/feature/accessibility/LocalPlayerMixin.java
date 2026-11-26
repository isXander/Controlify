/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.accessibility;

import dev.isxander.controlify.impl.Controlify;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
	private Object shouldUseAutoJump(Object keyboardAutoJump) {
		if (Controlify.instance().currentInputMode().isController()) {
			return Controlify.instance().getCurrentController()
					.map(controller -> controller.settings().generic.autoJump)
					.orElse(false);
		}
		return keyboardAutoJump;
	}
}
