/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.core;

import org.spongepowered.asm.mixin.Mixin;

//? if >=26.3 {
import com.mojang.blaze3d.platform.SDLEventHandler;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SDLEventHandler.class)
public class SDLEventHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = {
		"handleKeyEvent",
		"handleTextInputEvent",
		"handleTextEditingEvent",
		"handleTextEditingCandidatesEvent",
	}, at = @At("HEAD"))
	private void onKeyboardInput(CallbackInfo ci) {
		minecraft.execute(() -> {
			if (Controlify.instance().currentInputMode() != InputMode.MIXED) {
				Controlify.instance().setInputMode(InputMode.KEYBOARD_MOUSE);
			}
		});
	}

	@Inject(method = {
		"handleMouseMotionEvent",
		"handleMouseButtonEvent",
		"handleMouseWheelEvent",
	}, at = @At("HEAD"))
	private void onMouseInput(CallbackInfo ci) {
		minecraft.execute(() -> {
			if (Controlify.instance().currentInputMode() != InputMode.MIXED) {
				Controlify.instance().setInputMode(InputMode.KEYBOARD_MOUSE);
			} else {
				Controlify.instance().showCursorTemporarily();
			}
		});
	}
}
//?} else {
/*@Mixin(dev.isxander.controlify.utils.DummyMixinTarget.class)
public class SDLEventHandlerMixin { }
*///?}
