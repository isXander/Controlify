/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.fabric;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.fabric.platform.client.FabricPlatformClientImpl;
import dev.isxander.controlify.fabric.platform.FabricPlatformMainImpl;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.server.ControlifyServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

public class ControlifyBootstrap implements ClientModInitializer, ModInitializer, DedicatedServerModInitializer {
	@Override
	public void onInitializeClient() {
		PlatformClientUtil.IMPL = new FabricPlatformClientImpl();
		Controlify.instance().preInitialiseControlify();
	}

	@Override
	public void onInitializeServer() {
		ControlifyServer.getInstance().onInitializeServer();
	}

	@Override
	public void onInitialize() {
		PlatformMainUtil.IMPL = new FabricPlatformMainImpl();
		ControlifyServer.getInstance().onInitialize();
	}
}
