/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.sdl.SdlGamepad;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.Identifier;

import java.util.Map;

/// A regression test:
///
/// Tests the test framework's virtual button sending translates to the correct button IDs.
/// Without explicit mapping from logical button indices, sometimes a button would appear as
/// another on the mod side.
@SuppressWarnings("UnstableApiUsage")
public class VirtualControllerInputTests implements FabricClientGameTest {
	private static final Map<Integer, Identifier> XINPUT_BUTTONS = Map.ofEntries(
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_SOUTH, GamepadInputs.SOUTH_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_EAST, GamepadInputs.EAST_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_WEST, GamepadInputs.WEST_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_NORTH, GamepadInputs.NORTH_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_BACK, GamepadInputs.BACK_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_START, GamepadInputs.START_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_LEFT_STICK, GamepadInputs.LEFT_STICK_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK, GamepadInputs.RIGHT_STICK_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER, GamepadInputs.LEFT_SHOULDER_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER, GamepadInputs.RIGHT_SHOULDER_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP, GamepadInputs.DPAD_UP_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN, GamepadInputs.DPAD_DOWN_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_LEFT, GamepadInputs.DPAD_LEFT_BUTTON),
		Map.entry(SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT, GamepadInputs.DPAD_RIGHT_BUTTON)
	);

	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);

		try (var controller = controlify.virtualControllerBuilder()
				.withXbox()
				.attach()) {
			var controllerEntity = controller.getControllerEntity();
			var input = controllerEntity.input().orElseThrow();

			for (var expected : XINPUT_BUTTONS.entrySet()) {
				controller.holdButton(expected.getKey());
				context.waitFor(_ -> input.rawStateNow().isButtonDown(expected.getValue()), 10);

				for (var other : XINPUT_BUTTONS.values()) {
					if (!other.equals(expected.getValue()) && input.rawStateNow().isButtonDown(other)) {
						throw new AssertionError(
							"Pressed " + expected.getValue() + " but also received " + other
						);
					}
				}

				controller.releaseButton(expected.getKey());
				context.waitFor(_ -> !input.rawStateNow().isButtonDown(expected.getValue()), 10);
			}
		}

		CTestUtil.resetControlifySettings(context);
		CTestUtil.clearToasts(context);
		context.setScreen(TitleScreen::new);
	}
}
