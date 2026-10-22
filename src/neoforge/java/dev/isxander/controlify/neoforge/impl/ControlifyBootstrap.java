/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.neoforge.impl;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.gui.screen.ControlifySettingsScreen;
import dev.isxander.controlify.neoforge.impl.platform.NeoforgePlatformMainImpl;
import dev.isxander.controlify.neoforge.impl.platform.client.NeoforgePlatformClientImpl;
import dev.isxander.controlify.impl.platform.Environment;
import dev.isxander.controlify.impl.platform.client.PlatformClientUtil;
import dev.isxander.controlify.impl.platform.main.PlatformMainUtil;
import dev.isxander.controlify.impl.server.ControlifyServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

@Mod("controlify")
public class ControlifyBootstrap {
	public ControlifyBootstrap(IEventBus modBus) {
		PlatformMainUtil.IMPL = new NeoforgePlatformMainImpl();
		ControlifyServer.getInstance().onInitialize();

		if (PlatformMainUtil.getEnv() == Environment.CLIENT) {
			PlatformClientUtil.IMPL = new NeoforgePlatformClientImpl();

			ModLoadingContext.get().registerExtensionPoint(
				net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
				() -> (client, parent) -> new ControlifySettingsScreen(parent)
			);

			PlatformMainUtil.invokeControlifyEntrypoints();
			Controlify.instance().preInitialiseControlify();
		}
		if (PlatformMainUtil.getEnv() == Environment.SERVER) {
			ControlifyServer.getInstance().onInitializeServer();
		}
	}
}
