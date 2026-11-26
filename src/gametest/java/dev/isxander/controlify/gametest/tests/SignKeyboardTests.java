/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.api.InputMode;
import dev.isxander.controlify.api.gui.keyboard.InputTarget;
import dev.isxander.controlify.gametest.framework.TestUnitWorldContext;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.controlify.gametest.framework.controller.TestControllerContext;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.SignEditingBehaviour;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.sdl.SdlGamepad;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
//? if >=26.3
import net.minecraft.world.level.block.entity.SignTextSlot;

import java.util.List;

/// Edits a sign through the embedded on-screen keyboard: typing and deleting on every line,
/// changing lines with the right stick (wrapping both ways), then pressing done to save the sign.
@SuppressWarnings("UnstableApiUsage")
public class SignKeyboardTests implements FabricClientGameTest {
	private static final List<String> EXPECTED = List.of("line0!", "line1", "line2", "line");

	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);
		controlify.resetSettings();

		try (var controller = controlify.virtualControllerBuilder().withXbox().attach();
			var world = new TestUnitWorldContext(context);
			var region = world.allocateRegion(3, 3, 3)) {
			var signPos = new BlockPos(1, 1, 2);
			region.setBlock(signPos, Blocks.OAK_SIGN);

			context.runOnClient(_ -> {
				Controlify.instance().setCurrentController(controller.getControllerImpl(), true);
				Controlify.instance().setInputMode(InputMode.CONTROLLER);
			});
			region.runOnServer((level, player) -> {
				var pos = region.absolutePos(signPos);
				var sign = (SignBlockEntity) level.getBlockEntity(pos);
				((SignBlock) level.getBlockState(pos).getBlock()).openTextEdit(player, sign, /*? if >=26.3 {*/SignTextSlot.FRONT/*?} else {*//*true*//*?}*/);
			});
			context.waitForScreen(AbstractSignEditScreen.class);
			context.waitTick();

			var access = context.computeOnClient(_ -> (SignEditingBehaviour.Access) MinecraftUtil.getScreen());
			context.runOnClient(_ -> {
				var screen = MinecraftUtil.getScreen();
				check(access.controlify$getKeyboard() != null, "The sign editor should embed the on-screen keyboard");
				check(screen.getFocused() == access.controlify$getKeyboard(), "The keyboard should hold screen focus, but " + screen.getFocused() + " did");
				check(screen.children().stream().noneMatch(EditBox.class::isInstance), "The sign lines should not be reachable by navigation");
				check(screen.children().stream().noneMatch(Button.class::isInstance), "The done button should be hidden behind the keyboard");
			});

			// type and delete on every line, moving down after each
			for (int line = 0; line < 4; line++) {
				int index = line;
				context.runOnClient(_ -> {
					InputTarget target = (InputTarget) MinecraftUtil.getScreen();
					type(target, "line" + index + "x");
					backspace(target);
				});
				moveLine(context, controller, 1f);
			}
			// moving down from the last line wraps to the first
			context.runOnClient(_ -> type((InputTarget) MinecraftUtil.getScreen(), "!"));
			// moving up from the first line wraps to the last
			moveLine(context, controller, -1f);
			context.runOnClient(_ -> backspace((InputTarget) MinecraftUtil.getScreen()));

			context.runOnClient(_ -> {
				check(MinecraftUtil.getScreen().getFocused() == access.controlify$getKeyboard(),
						"The keyboard should keep focus while changing lines");
				checkLines(access.controlify$getSign(), "on the client");
			});

			// the done key's shortcut finishes editing, which saves the sign on the server
			controller.holdButton(SdlGamepad.SDL_GAMEPAD_BUTTON_START);
			context.waitTicks(2);
			controller.releaseButton(SdlGamepad.SDL_GAMEPAD_BUTTON_START);
			context.waitFor(_ -> MinecraftUtil.getScreen() == null, 20);
			waitForServerLines(context, region, signPos);
		} finally {
			controlify.resetSettings();
		}
	}

	private static void waitForServerLines(ClientGameTestContext context, TestUnitWorldContext.TestRegionContext region, BlockPos signPos) {
		List<String> lines = List.of();
		for (int tick = 0; tick < 40; tick++) {
			lines = region.computeOnServer((level, _) -> lines((SignBlockEntity) level.getBlockEntity(region.absolutePos(signPos))));
			if (lines.equals(EXPECTED)) return;
			context.waitTick();
		}
		throw new AssertionError("The sign should read " + EXPECTED + " on the server once saved, but read " + lines);
	}

	private static void moveLine(ClientGameTestContext context, TestControllerContext<?> controller, float direction) {
		controller.holdAxis(SdlGamepad.SDL_GAMEPAD_AXIS_RIGHTY, direction);
		context.waitTicks(2);
		controller.releaseAxis(SdlGamepad.SDL_GAMEPAD_AXIS_RIGHTY);
		context.waitTicks(2);
	}

	private static void type(InputTarget target, String text) {
		for (char c : text.toCharArray()) {
			target.acceptChar(c, 0);
		}
	}

	private static void backspace(InputTarget target) {
		target.acceptKeyCode(InputConstants.KEY_BACKSPACE, 0, 0);
	}

	private static List<String> lines(SignBlockEntity sign) {
		//? if >=26.3 {
		return sign.getText(SignTextSlot.FRONT).getMessages(false).stream().map(Component::getString).toList();
		//?} else {
		/*return java.util.Arrays.stream(sign.getFrontText().getMessages(false)).map(Component::getString).toList();
		*///?}
	}

	private static void checkLines(SignBlockEntity sign, String where) {
		List<String> lines = lines(sign);
		check(lines.equals(EXPECTED), "The sign should read " + EXPECTED + " " + where + ", but read " + lines);
	}

	private static void check(boolean condition, String message) {
		if (!condition) throw new AssertionError(message);
	}
}
