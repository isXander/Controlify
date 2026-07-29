/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.sign;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SignEditScreen.class)
public class SignEditScreenMixin {

	@ModifyReturnValue(method = "getSignYOffset", at = @At("RETURN"))
	private float modifySignY(float original) {
		return original - calculateOverlap();
	}


	//? if <26.2 {
	/*@Definition(id = "sign", method = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;sign(Lnet/minecraft/client/model/Model$Simple;FLnet/minecraft/world/level/block/state/properties/WoodType;IIII)V")
	@Expression("?.sign(?, ?, ?, ?, @(66), ?, @(168))")
	@ModifyExpressionValue(method = "extractSignBackground", at = @At("MIXINEXTRAS:EXPRESSION"))
	private int modifySignRenderY(int original) {
		return (int) (original - calculateOverlap());
	}
	*///?}


	@Unique private float calculateOverlap() {
		float original = 90f;

		float keyboardStart = ((Screen) (Object) this).height / 2f;
		float signEnd = original + 90;
		return Math.max(0, signEnd - keyboardStart);
	}

}
