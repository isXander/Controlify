/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.gametest.framework.TestUnitWorldContext;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.controlify.gametest.mixin.RecipeBookPageAccessor;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookComponentAccessor;
import dev.isxander.controlify.screenop.compat.vanilla.RecipeBookScreenProcessor;
import dev.isxander.controlify.utils.MinecraftUtil;
import dev.isxander.sdl.SdlGamepad;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

import java.util.function.BooleanSupplier;

/// Exercises the simulated mouse clicks used by recipe-book controller navigation.
@SuppressWarnings("UnstableApiUsage")
public class RecipeBookNavigationTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);
		controlify.resetSettings();

		try (var controller = controlify.virtualControllerBuilder().withXbox().attach();
			var world = new TestUnitWorldContext(context);
			var region = world.allocateRegion(3, 3, 3)) {
			var tablePos = new BlockPos(1, 1, 2);
			region.setBlock(tablePos, Blocks.CRAFTING_TABLE);
			region.runOnServer((level, player) -> {
				player.setGameMode(GameType.SURVIVAL);
				player.awardRecipes(level.getServer().getRecipeManager().getRecipes());
			});

			context.runOnClient(client -> {
				client.player.getRecipeBook().setOpen(RecipeBookType.CRAFTING, true);
				client.player.getRecipeBook().setFiltering(RecipeBookType.CRAFTING, false);
				Controlify.instance().setCurrentController(controller.getControllerEntity(), true);
				Controlify.instance().setInputMode(InputMode.CONTROLLER);
			});
			region.runOnServer((level, player) -> {
				var pos = region.absolutePos(tablePos);
				player.openMenu(level.getBlockState(pos).getMenuProvider(level, pos));
			});
			context.waitForScreen(CraftingScreen.class);

			var recipeBook = context.computeOnClient(_ ->
				((RecipeBookScreenProcessor.RecipeBookScreenAccessor) MinecraftUtil.getScreen())
					.controlify$getRecipeBookComponent());
			var component = (RecipeBookComponentAccessor) recipeBook;
			var page = context.computeOnClient(_ ->
				(RecipeBookPageAccessor) component.controlify$getRecipeBookPage());

			// Require real navigation targets so empty recipes or a closed book
			// cannot produce a misleading navigation failure.
			context.waitFor(_ -> recipeBook.isVisible() && page.controlify_test$getTotalPages() > 1);
			var tabs = context.computeOnClient(_ -> component.controlify$getTabButtons().stream()
				.filter(tab -> tab.visible).toList());
			context.runOnClient(_ -> {
				if (tabs.size() < 2 || component.controlify$getSelectedTab() != tabs.getFirst()
						|| page.controlify_test$getCurrentPage() != 0) {
					throw new AssertionError("Expected the first recipe category and page, with multiple visible categories");
				}
				if (!Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled()) {
					throw new AssertionError("Crafting screen must have virtual mouse enabled for recipe navigation");
				}
			});

			pressAndWait(context,
				() -> controller.holdButton(SdlGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER),
				() -> controller.releaseButton(SdlGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER),
				() -> page.controlify_test$getCurrentPage() == 1,
				"Right shoulder should advance the recipe page from 0 to 1");
			pressAndWait(context,
				() -> controller.holdButton(SdlGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER),
				() -> controller.releaseButton(SdlGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER),
				() -> page.controlify_test$getCurrentPage() == 0,
				"Left shoulder should return the recipe page from 1 to 0");
			pressAndWait(context,
				() -> controller.holdAxis(SdlGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER, 1f),
				() -> controller.releaseAxis(SdlGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER),
				() -> component.controlify$getSelectedTab() == tabs.get(1),
				"Right trigger should select the next recipe category");
			pressAndWait(context,
				() -> controller.holdAxis(SdlGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER, 1f),
				() -> controller.releaseAxis(SdlGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER),
				() -> component.controlify$getSelectedTab() == tabs.getFirst(),
				"Left trigger should select the previous recipe category");
		} finally {
			controlify.resetSettings();
		}
	}

	private static void pressAndWait(ClientGameTestContext context, Runnable press, Runnable release,
			BooleanSupplier condition, String message) {
		press.run();
		try {
			context.waitFor(_ -> condition.getAsBoolean(), 20);
		} catch (AssertionError failure) {
			throw new AssertionError(message, failure);
		} finally {
			release.run();
			context.waitTick();
		}
	}
}
