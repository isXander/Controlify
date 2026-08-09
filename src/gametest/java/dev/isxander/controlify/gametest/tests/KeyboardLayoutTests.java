package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.ControlifyGameTestContext;
import dev.isxander.sdl.SdlGamepad;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;

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
			world.getClientLevel().waitForChunksRender();

			try (var controller = controlify.virtualControllerBuilder()
					.withXbox()
					.attach()) {
				try (var _ = CTestUtil.applyResourcePack(context, RESOURCE_PACK_ID)) {
					// Test with default en_us language

					// open on-screen keyboard
					controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP);
					context.waitForScreen(ChatScreen.class);

					// check american keyboard layout
					context.assertScreenshotContains(TestScreenshotComparisonOptions
							.of("keyboard_layout_en_us")
							.save()
							.withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.001f)));

					// press back button to close on-screen keyboard
					controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_EAST);
					context.waitForScreen(null);

					// Test with en_gb language
					try (var _ = CTestUtil.setLanguage(context, "en_gb")) {
						// open on-screen keyboard
						controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP);
						context.waitForScreen(ChatScreen.class);

						// check british keyboard layout
						context.assertScreenshotContains(TestScreenshotComparisonOptions
							.of("keyboard_layout_en_gb")
							.save()
							.withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.001f)));
					}
					// change language without closing chat screen and check en_us loaded
					context.assertScreenshotContains(TestScreenshotComparisonOptions
						.of("keyboard_layout_en_us")
						.save()
						.withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.001f)));

					// press back button to close on-screen keyboard
					controller.tapButton(SdlGamepad.SDL_GAMEPAD_BUTTON_EAST);
					context.waitForScreen(null);
				}
			}
		}
	}
}
