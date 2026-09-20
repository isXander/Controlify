/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

/// Ensures that there are no limits to the amount of controllers the game can witness in its lifetime.
/// There is the worry that for example SDL can only issue a certain amount of IDs, like #SDL-16.
/// This makes sure that is not the case.
@SuppressWarnings("UnstableApiUsage")
public class ManyHotplugTests implements FabricClientGameTest {
	private static final int HOTPLUG_COUNT = 32;

	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);

		for (int i = 0; i < HOTPLUG_COUNT; i++) {
			try (var controller = controlify.virtualControllerBuilder()
					.withXbox()
					.attach()) {
				context.waitTicks(3);

				// finds within ControllerManager if it exists
				controller.getControllerEntity();
			}
			CTestUtil.clearToasts(context);
		}

		controlify.resetSettings();
	}
}
