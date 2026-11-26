/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame.look;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

/// Controlify's own look sources.
public final class BuiltinLook {
	public static final Identifier STICK = CUtil.rl("stick");
	public static final Identifier FLICK_STICK = CUtil.rl("flick_stick");
	public static final Identifier GYRO = CUtil.rl("gyro");

	static void register(LookRegistryImpl registry) {
		registry.registerSource(STICK, controller -> new StickLook(impl(controller)));
		registry.registerSource(FLICK_STICK, controller -> new FlickStick(impl(controller)));
		registry.registerSource(GYRO, controller -> new GyroLook(impl(controller)));
	}

	private static ControllerImpl impl(Controller controller) {
		return (ControllerImpl) controller;
	}

	private BuiltinLook() {
	}
}
