/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.gametest.framework.CEntityTypes;
import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.TestUnitWorldContext;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.controlify.gametest.framework.controller.TestControllerContext;
import dev.isxander.sdl.SdlGamepad;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.Vec3;

/// Tests the various in-game ways that a controller would receive a rumble effect.
/// To ensure all triggers are working properly.
///
/// Current untested rumble triggers are:
/// - fishing (fish biting)
/// - ender dragon death
@SuppressWarnings("UnstableApiUsage")
public class RumbleTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);

		try (var controller = controlify.virtualControllerBuilder()
				.withXbox()
				.attach()) {
			try (var world = new TestUnitWorldContext(context)) {
				world.getWorld().getClientLevel().waitForChunksRender();

				runTest(context, controlify, controller, world, "Lightning bolt", this::testLightningBoltRumble);
				runTest(context, controlify, controller, world, "Explosion", this::testExplosionRumble);
				runTest(context, controlify, controller, world, "Outgoing damage", this::testOutgoingDamage);
				runTest(context, controlify, controller, world, "Incoming damage", this::testIncomingDamage);
				runTest(context, controlify, controller, world, "Water fall", this::testWaterFall);
				runTest(context, controlify, controller, world, "Slow block", this::testSlowBlock);
				runTest(context, controlify, controller, world, "Wither spawn", this::testWitherSpawn);
				runTest(context, controlify, controller, world, "Block breaking", this::testBlockBreaking);
				runTest(context, controlify, controller, world, "Use item", this::testUseItem);
				runTest(context, controlify, controller, world, "Item break", this::testItemBreak);
			}
		}
	}

	/// Summons lightning bolt near the player, which should cause a rumble effect.
	private void testLightningBoltRumble(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		try (var region = world.allocateRegion(5, 5, 5)) {
			var bolt = region.spawn(CEntityTypes.LIGHTNING_BOLT, region.getPlayerBlockPos());
			bolt.setVisualOnly(true);

			waitForRumble(context, controller);
		}

	}

	/// Summons an explosion near the player, which should cause a rumble.
	/// Summons a second, distant, explosion, which should cause a lesser rumble.
	private void testExplosionRumble(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		// Test that explosion causes rumble

		float highFreqRumble, lowFreqRumble;
		try (var region = world.allocateRegion(5, 5, 5)) {
			region.runOnServer((level, player) -> {
				level.explode(player, player.getX(), player.getY(), player.getZ(), 6f, Level.ExplosionInteraction.NONE);
			});

			waitForRumble(context, controller);

			highFreqRumble = controller.state().getHighFrequencyRumble();
			lowFreqRumble = controller.state().getLowFrequencyRumble();
		}

		cleanupTest(context, controlify, controller, world);

		// Test that distant explosion causes lesser rumble

		try (var region = world.allocateRegion(5, 5, 5)) {
			region.runOnServer((level, player) -> {
				level.explode(player, player.getX(), player.getY() + 3, player.getZ(), 6f, Level.ExplosionInteraction.NONE);
			});

			waitForRumble(context, controller);

			if (controller.state().getHighFrequencyRumble() > highFreqRumble
				|| controller.state().getLowFrequencyRumble() > lowFreqRumble) {
				throw new AssertionError("Distant explosion rumble should rumble with less intensity.");
			}
		}
	}

	/// Makes the player apply damage to a mob, which should cause a rumble.
	private void testOutgoingDamage(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		// Attack cow and wait for rumble

		try (var region = world.allocateRegion(5, 5, 5)) {
			var cow = region.spawnWithNoFreeWill(CEntityTypes.COW, region.getPlayerBlockPos());

			context.waitTicks(10);

			var cowPos = region.computeOnServer((_, _) -> cow.getBoundingBox().getCenter());
			context.runOnClient(minecraft -> minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, cowPos));

			context.waitTicks(2);

			controller.tapAxis(SdlGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER, 1f);

			waitForRumble(context, controller);
		}
	}

	/// Makes a mob apply damage to the player, which should cause a rumble.
	private void testIncomingDamage(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		// Make slime attack player and wait for rumble

		try (var region = world.allocateRegion(5, 5, 5)) {
			var slime = region.spawnWithNoFreeWill(CEntityTypes.SLIME, region.getPlayerBlockPos());
			context.waitTick();
			region.attack(slime, region.getPlayer());

			waitForRumble(context, controller);

			context.waitTicks(10);
		}
	}

	private void testWaterFall(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		try (var region = world.allocateRegion(5, 25, 5)) {
			region.setBlock(2, 2, 2, Blocks.WATER);
			context.waitTick();
			region.teleportPlayer(new BlockPos(2, 20, 2));

			waitForRumble(context, controller);
		}
	}

	private void testSlowBlock(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		try (var region = world.allocateRegion(5, 5, 5)) {
			region.teleportPlayer(new BlockPos(2, 2, 2));
			region.setBlock(2, 1, 2, Blocks.SOUL_SAND);

			context.waitTick();

			// walk forward
			controller.holdAxis(SdlGamepad.SDL_GAMEPAD_AXIS_LEFTY, -1);

			// now rumbling
			waitForRumble(context, controller);

			controller.releaseAxis(SdlGamepad.SDL_GAMEPAD_AXIS_LEFTY);
		}
	}

	private void testWitherSpawn(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		try (var region = world.allocateRegion(5, 5, 5)) {
			region.runOnServer((_, player) -> player.setGameMode(GameType.CREATIVE));
			var wither = region.spawn(CEntityTypes.WITHER, region.getPlayerBlockPos());
			wither.setInvulnerableTicks(2);

			waitForRumble(context, controller);
		}
	}

	private void testBlockBreaking(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		try (var region = world.allocateRegion(5, 5, 5)) {
			var blockPos = new BlockPos(2, 1, 2);
			region.setBlock(blockPos, Blocks.DIRT);

			context.waitTick();

			context.runOnClient(minecraft ->
					minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(region.absolutePos(blockPos))));

			context.waitTicks(2);

			controller.holdAxis(SdlGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER, 1f);

			waitForRumble(context, controller);

			controller.releaseAxis(SdlGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER);
		}
	}

	private void testUseItem(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		try (var region = world.allocateRegion(5, 5, 5)) {
			region.runOnServer((_, player) -> {
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
				player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW));
			});

			context.waitTick();

			controller.holdAxis(SdlGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER, 1f);

			waitForRumble(context, controller);

			controller.releaseAxis(SdlGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER);
		}
	}

	private void testItemBreak(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		try (var region = world.allocateRegion(5, 5, 5)) {
			var campfirePos = new BlockPos(2, 1, 2);
			region.setBlock(
					campfirePos,
					Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false)
			);
			region.runOnServer((_, player) -> {
				var flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);
				flintAndSteel.setDamageValue(flintAndSteel.getMaxDamage() - 1);
				player.setItemInHand(InteractionHand.MAIN_HAND, flintAndSteel);
			});

			context.waitTick();

			context.runOnClient(minecraft ->
					minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atBottomCenterOf(region.absolutePos(campfirePos))));

			context.waitTicks(2);

			controller.holdAxis(SdlGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER, 1f);

			waitForRumble(context, controller);

			controller.releaseAxis(SdlGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER);

			if (!region.computeOnServer((_, player) -> player.getMainHandItem().isEmpty())) {
				throw new AssertionError("Flint and steel did not break");
			}
		}
	}

	private void waitForRumble(ClientGameTestContext context, TestControllerContext<?> controller, int timeout) {
		try {
			context.waitFor(_ -> controller.state().hasRumble(), timeout);
		} catch (AssertionError e) {
			throw new AssertionError("Timed out waiting for rumble", e);
		}
	}

	private void waitForRumble(ClientGameTestContext context, TestControllerContext<?> controller) {
		waitForRumble(context, controller, ClientGameTestContext.DEFAULT_TIMEOUT);
	}

	private void cleanupTest(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world
	) {
		// Wait for rumble to stop
		context.waitFor(_ -> !controller.state().hasRumble());
	}

	private void runTest(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestUnitWorldContext world,
		String testName,
		RumbleTest test
	) {
		try {
			test.run(context, controlify, controller, world);
		} catch (Throwable e) {
			throw new AssertionError("Test '" + testName + "' failed", e);
		}

		cleanupTest(context, controlify, controller, world);
	}

	private interface RumbleTest {
		void run(ClientGameTestContext context, ControlifyGameTestContext controlify, TestControllerContext<?> controller, TestUnitWorldContext world);
	}
}
