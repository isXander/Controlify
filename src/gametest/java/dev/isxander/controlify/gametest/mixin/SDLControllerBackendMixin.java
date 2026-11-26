/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.controlify.impl.controller.ControllerInfo;
import dev.isxander.controlify.impl.controllermanager.SDLControllerBackend;
import dev.isxander.controlify.impl.controllermanager.SDLUniqueControllerID;
import dev.isxander.sdl.Sdl;
import dev.isxander.sdl.SdlJoystickId;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SDLControllerBackend.class)
public class SDLControllerBackendMixin {
	@Shadow
	@Final
	private Sdl sdl;

	/// Only load virtual joysticks in test environments
	@WrapMethod(method = "createAndAddController(Ldev/isxander/controlify/impl/controller/ControllerInfo;Z)V")
	private void preventNonVirtual(
		ControllerInfo info,
		boolean hotplugged,
		Operation<Void> original
	) {
		SdlJoystickId jid = ((SDLUniqueControllerID) info.ucid()).jid();

		if (!sdl.joystick().SDL_IsJoystickVirtual(jid)) {
			return;
		}

		original.call(info, hotplugged);
	}
}
