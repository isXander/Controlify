/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.core;

import org.spongepowered.asm.mixin.Mixin;

//? if >=26.3 {
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.sdl.SDLMouse;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InputConstants.class)
public class InputConstantsMixin {
	@Inject(method = "releaseMouse", at = @At("HEAD"))
	private static void ensureMouseVisible(CallbackInfo ci) {
		SDLMouse.SDL_ShowCursor();
	}
}
//?} else {
/*@Mixin(dev.isxander.controlify.utils.DummyMixinTarget.class)
public class InputConstantsMixin {
}
*///?}
