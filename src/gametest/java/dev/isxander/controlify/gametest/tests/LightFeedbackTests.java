/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.api.feedback.FeedbackEvent;
import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.controlify.gametest.framework.controller.TestControllerContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.resources.Identifier;

/// Verifies that a light feedback effect reaches the controller's RGB LED.
/// The effect comes from a test resource pack, played by an event that is never registered.
@SuppressWarnings("UnstableApiUsage")
public class LightFeedbackTests implements FabricClientGameTest, ClientModInitializer {
	private static final Identifier PACK = CTestUtil.id("light_feedback_test");
	/// Plays `controlify_test:light_test`, a fully opaque red light layer, from the test pack.
	private static final FeedbackEvent<Void> LIGHT_TEST = FeedbackEvent.builder(CTestUtil.id("light_test")).build();

	@Override
	public void onInitializeClient() {
		CTestUtil.registerBuiltinResourcePack(PACK);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);

		try (var controller = controlify.virtualControllerBuilder()
				.withDualsense()
				.attach()) {
			if (controller.getControllerImpl().led().isEmpty()) {
				throw new AssertionError("The virtual DualSense has no LED component, so light feedback has nowhere to go");
			}
			if (!controller.getControllerImpl().settings().light.enabled) {
				throw new AssertionError("Light effects are disabled in the controller's settings");
			}

			try (var world = context.worldBuilder().create();
				var _ = CTestUtil.applyResourcePack(context, PACK)) {
				//? if >=26.2 {
				world.getConnection().waitForChunksRender();
				//?} else {
				//world.getClientLevel().waitForChunksRender();
				//?}

				// hold the event every tick until the LED turns red
				try {
					context.waitFor(_ -> {
						LIGHT_TEST.hold();
						return ledIs(controller, 0xFF, 0x00, 0x00);
					});
				} catch (AssertionError e) {
					throw new AssertionError("The LED never turned red while the light effect was held; it is " + ledHex(controller), e);
				}

				// once the event is no longer held, the light falls back to its default, which is off
				try {
					context.waitFor(_ -> ledIs(controller, 0x00, 0x00, 0x00));
				} catch (AssertionError e) {
					throw new AssertionError("The LED did not return to off after the light effect stopped; it is " + ledHex(controller), e);
				}
			}
		}
	}

	private static boolean ledIs(TestControllerContext<?> controller, int red, int green, int blue) {
		var state = controller.state();
		return (state.getLedRed() & 0xFF) == red
				&& (state.getLedGreen() & 0xFF) == green
				&& (state.getLedBlue() & 0xFF) == blue;
	}

	private static String ledHex(TestControllerContext<?> controller) {
		var state = controller.state();
		return String.format("#%02x%02x%02x", state.getLedRed() & 0xFF, state.getLedGreen() & 0xFF, state.getLedBlue() & 0xFF);
	}
}
