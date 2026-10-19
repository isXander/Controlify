/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controllermanager.SDLControllerManager;
import dev.isxander.controlify.impl.controllermanager.SDLUniqueControllerID;
import dev.isxander.controlify.impl.controllermanager.UniqueControllerID;
import dev.isxander.controlify.impl.hid.ControllerHIDInfo;
import dev.isxander.controlify.impl.utils.log.ControlifyLogger;
import dev.isxander.sdl.Sdl;
import dev.isxander.sdl.SdlJoystickId;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(SDLControllerManager.class)
public class SDLControllerManagerMixin {
	@Shadow
	@Final
	private Sdl sdl;

	/// Only load virtual joysticks in test environments
	@WrapMethod(method = "createController")
	private Optional<ControllerImpl> preventNonVirtual(
		UniqueControllerID ucid,
		ControllerHIDInfo hidInfo,
		ControlifyLogger controllerLogger,
		Operation<Optional<ControllerImpl>> original
	) {
		SdlJoystickId jid = ((SDLUniqueControllerID) ucid).jid();

		if (!sdl.joystick().SDL_IsJoystickVirtual(jid)) {
			controllerLogger.warn("Suppressing creation of controller as the test environment permits only virtual controllers");
			return Optional.empty();
		}

		return original.call(ucid, hidInfo, controllerLogger);
	}
}
