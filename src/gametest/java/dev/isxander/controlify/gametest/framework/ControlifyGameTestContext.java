/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.gametest.mixin.SDLControllerManagerAccessor;
import dev.isxander.sdl.Sdl;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

@SuppressWarnings("UnstableApiUsage")
public class ControlifyGameTestContext {
	private final ClientGameTestContext context;
	private final Sdl sdl;

	public ControlifyGameTestContext(ClientGameTestContext context) {
		this.context = context;
		this.sdl = ((SDLControllerManagerAccessor) Controlify.instance().getControllerManager().orElseThrow()).controlify_test$getSdl();
	}

	public VirtualControllerBuilder virtualControllerBuilder() {
		return new VirtualControllerBuilder(this.context, this, this.sdl);
	}

	public void setInputMode(InputMode inputMode) {
		Controlify.instance().setInputMode(inputMode);
		context.waitTick();
	}
}
