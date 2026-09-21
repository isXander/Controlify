/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.controlify.screenop.keyboard.KeyboardWidget;
import dev.isxander.controlify.utils.MinecraftUtil;
import dev.isxander.sdl.SdlGamepad;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class KeyboardLayoutTests implements FabricClientGameTest, ClientModInitializer {
	private static final Identifier RESOURCE_PACK_ID = CTestUtil.id("keyboard_layout_test");

	@Override
	public void onInitializeClient() {
		CTestUtil.registerBuiltinResourcePack(RESOURCE_PACK_ID);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);

		try (var world = context.worldBuilder()
				.create()) {
			//? if >=26.2 {
			world.getConnection().waitForChunksRender();
			//?} else {
			/*world.getClientLevel().waitForChunksRender();
			*///?}

			runLocaleTest(context, controlify);
		}
	}

	/// Ensures that the keyboard layout changes appropriately based on the
	/// current language setting of the client.
	///
	/// Applies a resource pack that makes en_us and en_gb very clearly different,
	/// and swaps locales, comparing screenshots.
	private void runLocaleTest(ClientGameTestContext context, ControlifyGameTestContext controlify) {
		try (var controller = controlify.virtualControllerBuilder()
				.withXbox()
				.attach()) {
			try (var _ = CTestUtil.applyResourcePack(context, RESOURCE_PACK_ID)) {
				// Test with default en_us language

				// open on-screen keyboard
				controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP);
				context.waitForScreen(ChatScreen.class);

				// check american keyboard layout
				assertKeyboard(context, "en_us");

				// press back button to close on-screen keyboard
				controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_EAST);
				context.waitForScreen(null);

				// Test with en_gb language
				try (var _ = CTestUtil.setLanguage(context, "en_gb")) {
					// open on-screen keyboard
					controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP);
					context.waitForScreen(ChatScreen.class);

					// check british keyboard layout
					assertKeyboard(context, "en_gb");
				}
				// change language without closing chat screen and check en_us loaded
				assertKeyboard(context, "en_us");

				// press back button to close on-screen keyboard
				controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_EAST);
				context.waitForScreen(null);
			}
		}

	}

	private static void assertKeyboard(ClientGameTestContext context, String keyString) {
		context.computeOnClient(_ -> {
			var chatScreen = (ChatScreen) MinecraftUtil.getScreen();
			KeyboardWidget keyboard = chatScreen.children().stream()
					.flatMap(child -> child instanceof KeyboardWidget k ? Stream.of(k) : Stream.empty())
					.findAny()
					.orElse(null);
			if (keyboard == null) {
				return false;
			}

			return keyboard.children().stream()
					.anyMatch(key -> key.getKeyFunction().displayName().getString().equals(keyString));
		});
	}
}
