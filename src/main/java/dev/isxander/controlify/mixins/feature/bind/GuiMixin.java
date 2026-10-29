/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.bind;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.input.InputComponentImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
		//? if >=26.2 {
		Gui.class
		//?} else {
		//Minecraft.class
		//?}
)
public class GuiMixin {
	@Inject(method = "setScreen", at = @At("HEAD"))
	private void notifyBindGuiOutputOfScreenChange(CallbackInfo ci) {
		Controlify.instance().getCurrentController().map(ControllerImpl::input)
				.ifPresent(InputComponentImpl::onGuiNavigate);
	}
}
