/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.controlify.impl.gui.keyboard.KeyboardWidget;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.sdl.SdlGamepad;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import dev.isxander.controlify.gametest.framework.controller.TestControllerContext;
import dev.isxander.controlify.impl.gui.keyboard.KeyWidget;
import dev.isxander.controlify.impl.gui.keyboard.KeyboardLayout;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.function.Predicate;
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
			//world.getClientLevel().waitForChunksRender();
			//?}

			runLocaleTest(context, controlify);
			runChatTypingTest(context, controlify);
		}
	}

	/// Ensures that the keyboard layout changes appropriately based on the
	/// current language setting of the client.
	///
	/// Applies a resource pack that makes en_us and en_gb very clearly different,
	/// and swaps locales, checking the displayed keys.
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

	/// Regression test: types into the chat box with the on-screen keyboard using only controller
	/// input, then deletes with the keyboard's backspace key. Synthetic key events once carried a
	/// zero keycode, so backspace silently did nothing while character keys worked.
	private void runChatTypingTest(ClientGameTestContext context, ControlifyGameTestContext controlify) {
		try (var controller = controlify.virtualControllerBuilder()
				.withXbox()
				.attach()) {
			// open chat with the on-screen keyboard
			controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP);
			context.waitForScreen(ChatScreen.class);
			context.waitFor(_ -> findKeyboard() != null && findChatBox() != null);

			String typed = "hey";
			for (char c : typed.toCharArray()) {
				String expected = String.valueOf(c);
				pressKey(context, controller, k ->
						k.getKeyFunction() instanceof KeyboardLayout.KeyFunction.StringFunc(String string, var ignored)
								&& string.equals(expected));
			}
			assertChatText(context, typed);

			pressKey(context, controller, k ->
					k.getKeyFunction() instanceof KeyboardLayout.KeyFunction.SpecialFunc(var action)
							&& action == KeyboardLayout.KeyFunction.SpecialFunc.Action.BACKSPACE);
			assertChatText(context, typed.substring(0, typed.length() - 1));

			// close the on-screen keyboard
			controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_EAST);
			context.waitForScreen(null);
		}
	}

	private static void assertChatText(ClientGameTestContext context, String expected) {
		try {
			context.waitFor(_ -> {
				EditBox box = findChatBox();
				return box != null && box.getValue().equals(expected);
			}, 40);
		} catch (AssertionError e) {
			String actual = context.computeOnClient(_ -> {
				EditBox box = findChatBox();
				return box == null ? "<no chat box>" : box.getValue();
			});
			throw new AssertionError("Expected chat box to contain '" + expected + "' but was '" + actual + "'", e);
		}
	}

	/// Navigates focus to the first key matching the predicate using the d-pad,
	/// then activates it with the confirm button.
	private static void pressKey(ClientGameTestContext context, TestControllerContext<?> controller, Predicate<KeyWidget> target) {
		for (int attempt = 0; ; attempt++) {
			if (attempt > 40) {
				throw new AssertionError("Could not navigate to the requested keyboard key");
			}
			int[] move = context.computeOnClient(_ -> {
				KeyboardWidget keyboard = findKeyboard();
				if (keyboard == null || keyboard.getFocused() == null) {
					return new int[]{-1};
				}
				Optional<KeyWidget> wanted = keyboard.children().stream().filter(target).findFirst();
				if (wanted.isEmpty()) {
					throw new AssertionError("No keyboard key matches the requested function");
				}
				KeyWidget from = keyboard.getFocused();
				KeyWidget to = wanted.get();
				if (from == to) {
					return new int[]{0};
				}
				int dx = (to.getX() + to.getWidth() / 2) - (from.getX() + from.getWidth() / 2);
				int dy = (to.getY() + to.getHeight() / 2) - (from.getY() + from.getHeight() / 2);
				// move vertically first so that we land on the right row
				if (Math.abs(dy) > from.getHeight() / 2) {
					return new int[]{1, dy < 0 ? SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP : SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN};
				}
				return new int[]{1, dx < 0 ? SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_LEFT : SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT};
			});

			if (move[0] == 0) {
				break;
			}
			if (move[0] == -1) {
				context.waitTick();
				continue;
			}

			KeyWidget before = context.computeOnClient(_ -> findKeyboard().getFocused());
			controller.tapButton(move[1]);
			context.waitFor(_ -> {
				KeyboardWidget keyboard = findKeyboard();
				return keyboard != null && keyboard.getFocused() != before;
			}, 40);
		}

		// let the newly focused key settle so its held-press guard is clear
		context.waitTicks(2);
		controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_SOUTH);
	}

	private static KeyboardWidget findKeyboard() {
		if (!(MinecraftUtil.getScreen() instanceof ChatScreen chatScreen)) {
			return null;
		}
		return chatScreen.children().stream()
				.flatMap(child -> child instanceof KeyboardWidget k ? Stream.of(k) : Stream.empty())
				.findAny()
				.orElse(null);
	}

	private static EditBox findChatBox() {
		if (!(MinecraftUtil.getScreen() instanceof ChatScreen chatScreen)) {
			return null;
		}
		return chatScreen.children().stream()
				.flatMap(child -> child instanceof EditBox e ? Stream.of(e) : Stream.empty())
				.findAny()
				.orElse(null);
	}

	private static void assertKeyboard(ClientGameTestContext context, String keyString) {
		context.waitFor(_ -> {
			if (!(MinecraftUtil.getScreen() instanceof ChatScreen chatScreen)) {
				return false;
			}
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
