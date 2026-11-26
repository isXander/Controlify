/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.virtualmouse;

import dev.isxander.controlify.impl.screenop.ScreenRuntime;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(
		//? if >=26.2 {
		net.minecraft.client.gui.Gui.class
		//?} else {
		//net.minecraft.client.Minecraft.class
		//?}
)
public class GuiMixin {
	@Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;updateTitle()V"))
	private void onScreenChanged(Screen screen, CallbackInfo ci) {
		ScreenRuntime.onScreenChanged();
	}
}
