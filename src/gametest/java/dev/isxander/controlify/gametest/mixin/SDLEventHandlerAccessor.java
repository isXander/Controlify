/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.mixin;

import org.spongepowered.asm.mixin.Mixin;

//? if >=26.3 {
import com.mojang.blaze3d.platform.SDLEventHandler;
import org.lwjgl.sdl.SDL_Event;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SDLEventHandler.class)
public interface SDLEventHandlerAccessor {
	@Invoker("handleMouseMotionEvent")
	void controlify_test$handleMouseMotionEvent(SDL_Event event);
}
//?} else {
/*@Mixin(dev.isxander.controlify.utils.DummyMixinTarget.class)
public interface SDLEventHandlerAccessor { }
*///?}
