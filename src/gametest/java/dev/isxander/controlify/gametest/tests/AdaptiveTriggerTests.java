/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.driver.dualsense.DualsenseEffectsState;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.controlify.gametest.framework.controller.DualsenseEffectDriver;
import dev.isxander.controlify.gametest.framework.controller.TestControllerContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;

/// Verifies built-in and resource-pack adaptive trigger rules reach a DualSense
/// controller, including server-supplied item tags.
@SuppressWarnings("UnstableApiUsage")
public class AdaptiveTriggerTests implements FabricClientGameTest, ClientModInitializer {
	private static final Identifier LOW_PRIORITY_PACK = CTestUtil.id("adaptive_trigger_low_priority");
	private static final Identifier HIGH_PRIORITY_PACK = CTestUtil.id("adaptive_trigger_high_priority");
	private static final Identifier SERVER_TAG_PACK = CTestUtil.id("adaptive_trigger_server_tag");
	private static final TagKey<Item> SERVER_TRIGGER_TAG = TagKey.create(
		Registries.ITEM,
		CTestUtil.id("adaptive_trigger_test")
	);

	@Override
	public void onInitializeClient() {
		CTestUtil.registerBuiltinResourcePack(LOW_PRIORITY_PACK);
		CTestUtil.registerBuiltinResourcePack(HIGH_PRIORITY_PACK);
		CTestUtil.registerBuiltinResourcePack(SERVER_TAG_PACK);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);

		try (var controller = controlify.virtualControllerBuilder()
				.withDualsense()
				.attachWithDriver(new DualsenseEffectDriver())) {
			try (var world = context.worldBuilder().create()) {
				world.getClientLevel().waitForChunksRender();

				setServerHands(world, new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.SHIELD));
				context.waitTick();

				testTriggerEffectsDisabled(context, controller);

				controller.getControllerEntity().settings().dualsense.triggerEffects = true;

				context.waitTick();

				DualsenseEffectsState expected = new DualsenseEffectsState();
				expected.rgucLeftTriggerEffect = new DualsenseTriggerEffect.Feedback((byte) 3, (byte) 3).createState();
				expected.rgucRightTriggerEffect = new DualsenseTriggerEffect.Weapon((byte) 3, (byte) 5, (byte) 1).createState();
				waitForEffects(context, controller, expected);

				testResourcePackPriority(context, world, controller);
				testServerTagRule(context, world, controller);

				controller.getControllerEntity().settings().dualsense.triggerEffects = false;
				context.waitTick();
				waitForEffects(context, controller, new DualsenseEffectsState());
			}
		}

		controlify.resetSettings();
	}

	/// Tests that when the trigger effect option is disabled,
	/// no trigger effects reach the controller.
	private static void testTriggerEffectsDisabled(
		ClientGameTestContext context,
		TestControllerContext<DualsenseEffectsState> controller
	) {
		controller.getControllerEntity().settings().dualsense.triggerEffects = false;
		context.waitTick();
		waitForEffects(context, controller, new DualsenseEffectsState());
	}

	/// Test that higher priority resource packs are prioritized correctly.
	/// This is a regression test when the trigger effect manager processed the
	/// resource stack in the opposite direction.
	private static void testResourcePackPriority(
		ClientGameTestContext context,
		TestSingleplayerContext world,
		TestControllerContext<DualsenseEffectsState> controller
	) {
		setServerHands(world, new ItemStack(Items.DIAMOND_SWORD), ItemStack.EMPTY);

		try (var _ = CTestUtil.applyResourcePack(context, LOW_PRIORITY_PACK)) {
			DualsenseEffectsState lowPriorityExpected = new DualsenseEffectsState();
			lowPriorityExpected.rgucRightTriggerEffect = new DualsenseTriggerEffect.Feedback((byte) 2, (byte) 2).createState();
			context.waitTick();
			waitForEffects(context, controller, lowPriorityExpected);

			try (var _ = CTestUtil.applyResourcePack(context, HIGH_PRIORITY_PACK)) {
				DualsenseEffectsState highPriorityExpected = new DualsenseEffectsState();
				highPriorityExpected.rgucRightTriggerEffect = new DualsenseTriggerEffect.Vibration((byte) 1, (byte) 4, (byte) 11).createState();
				context.waitTick();
				waitForEffects(context, controller, highPriorityExpected);
			}
		}
	}

	/// Tests that a server-provided tag is synchronized and used by the trigger effect manager.
	private static void testServerTagRule(
		ClientGameTestContext context,
		TestSingleplayerContext world,
		TestControllerContext<DualsenseEffectsState> controller
	) {
		setServerHands(world, new ItemStack(Items.CARROT_ON_A_STICK), ItemStack.EMPTY);
		context.waitTick();
		context.runOnClient(minecraft -> {
			if (!minecraft.player.getMainHandItem().is(SERVER_TRIGGER_TAG)) {
				throw new AssertionError("Server item tag was not synchronized to the client");
			}
		});

		try (var _ = CTestUtil.applyResourcePack(context, SERVER_TAG_PACK)) {
			DualsenseEffectsState expected = new DualsenseEffectsState();
			expected.rgucRightTriggerEffect = new DualsenseTriggerEffect.Weapon((byte) 2, (byte) 8, (byte) 6).createState();
			context.waitTick();
			waitForEffects(context, controller, expected);
		}
	}

	private static void setServerHands(TestSingleplayerContext world, ItemStack mainHand, ItemStack offHand) {
		world.getServer().runOnServer(server -> {
			var player = CTestUtil.getPrincipalPlayer(server);
			player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
			player.setItemInHand(InteractionHand.OFF_HAND, offHand);
		});
	}

	private static void waitForEffects(
		ClientGameTestContext context,
		TestControllerContext<DualsenseEffectsState> controller,
		DualsenseEffectsState expected
	) {
		context.waitFor(_ -> {
			DualsenseEffectsState actual = controller.state().getGamepadEffectState();
			return actual != null
				&& sameEffect(actual.rgucLeftTriggerEffect, expected.rgucLeftTriggerEffect)
				&& sameEffect(actual.rgucRightTriggerEffect, expected.rgucRightTriggerEffect);
		});
	}

	private static boolean sameEffect(
		DualsenseEffectsState.TriggerEffect actual,
		DualsenseEffectsState.TriggerEffect expected
	) {
		return actual.effectType == expected.effectType
			&& Arrays.equals(actual.parameters, expected.parameters);
	}
}
