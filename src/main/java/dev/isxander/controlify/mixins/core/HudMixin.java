/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.ingame.InGameInputHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
		//? if >=26.4 {
		net.minecraft.client.gui.components.PlayerTabOverlay.class
		//?} else if >=26.2 {
		//net.minecraft.client.gui.Hud.class
		//?} else {
		//net.minecraft.client.gui.Gui.class
		//?}
)
public class HudMixin {
	// 26.4 moved the player list key check from the HUD into the tab overlay itself
	@ModifyExpressionValue(
			method = /*? if >=26.4 {*/"extractRenderState"/*?} else {*//*"extractTabList"*//*?}*/,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z")
	)
	private boolean shouldShowPlayerList(boolean keyDown) {
		return keyDown || Controlify.instance().inGameInputHandler().map(InGameInputHandler::shouldShowPlayerList).orElse(false);
	}
}
