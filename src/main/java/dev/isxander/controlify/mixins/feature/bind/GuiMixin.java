/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.bind;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.InputComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
		//? if >=26.2 {
		net.minecraft.client.gui.Gui.class
		//?} else {
		/*net.minecraft.client.Minecraft.class
		*///?}
)
public class GuiMixin {
	@Inject(method = "setScreen", at = @At("HEAD"))
	private void notifyBindGuiOutputOfScreenChange(CallbackInfo ci) {
		ControlifyApi.get().getCurrentController().flatMap(ControllerEntity::input)
				.ifPresent(InputComponent::notifyGuiPressOutputsOfNavigate);
	}
}
