/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.gametest.framework.CEntityTypes;
import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.ControlifyGameTestContext;
import dev.isxander.controlify.gametest.framework.TestControllerContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

@SuppressWarnings("UnstableApiUsage")
public class RumbleTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);

		try (var controller = controlify.virtualControllerBuilder()
				.withXbox()
				.attach()) {
			try (var world = context.worldBuilder()
					.setUseConsistentSettings(true)
					.create()) {
				world.getClientLevel().waitForChunksDownload();

				runTest(context, controlify, controller, world, "Lightning bolt", this::testLightningBoltRumble);
				runTest(context, controlify, controller, world, "Explosion", this::testExplosionRumble);
				runTest(context, controlify, controller, world, "Outgoing damage", this::testOutgoingDamage);
				runTest(context, controlify, controller, world, "Incoming damage", this::testIncomingDamage);
			}
		}
	}

	private void testLightningBoltRumble(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestSingleplayerContext world
	) {
		world.getServer().runOnServer(server -> {
			ServerPlayer player = CTestUtil.getPrincipalPlayer(server);
			ServerLevel level = player.level();

			LightningBolt bolt = CEntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.COMMAND);
			bolt.snapTo(player.position());
			bolt.setVisualOnly(true);
			level.addFreshEntity(bolt);
		});

		context.waitFor(_ -> controller.state().hasRumble(), 10);
	}

	private void testExplosionRumble(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestSingleplayerContext world
	) {
		// Test that explosion causes rumble


		world.getServer().runOnServer(server -> {
			ServerPlayer player = CTestUtil.getPrincipalPlayer(server);
			ServerLevel level = player.level();

			level.explode(player, player.getX(), player.getY(), player.getZ(), 6f, Level.ExplosionInteraction.NONE);
		});

		context.waitFor(_ -> controller.state().hasRumble(), 10);
		float highFreqRumble = controller.state().getHighFrequencyRumble();
		float lowFreqRumble = controller.state().getLowFrequencyRumble();

		cleanupTest(context, controlify, controller, world);


		// Test that distant explosion causes lesser rumble


		world.getServer().runOnServer(server -> {
			ServerPlayer player = CTestUtil.getPrincipalPlayer(server);
			ServerLevel level = player.level();

			level.explode(player, player.getX(), player.getY() + 10.0, player.getZ(), 6f, Level.ExplosionInteraction.NONE);
		});

		context.waitFor(_ -> controller.state().hasRumble(), 10);
		if (controller.state().getHighFrequencyRumble() > highFreqRumble
				|| controller.state().getLowFrequencyRumble() > lowFreqRumble) {
			throw new AssertionError("Distant explosion rumble should rumble with less intensity.");
		}
	}

	private void testOutgoingDamage(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestSingleplayerContext world
	) {
		// Attack cow and wait for rumble


		var cow = world.getServer().computeOnServer(server ->
			CTestUtil.summonEntityAtPlayer(server, CEntityTypes.COW, c -> c.setNoAi(true)));

		context.waitTick();

		context.runOnClient(minecraft -> minecraft.player.attack(cow));

		context.waitFor(_ -> controller.state().hasRumble(), 10);
	}

	private void testIncomingDamage(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestSingleplayerContext world
	) {
		// Make slime attack player and wait for rumble


		world.getServer().runOnServer(server -> {
			ServerPlayer player = CTestUtil.getPrincipalPlayer(server);
			var slime = CTestUtil.summonEntityAtPlayer(server, CEntityTypes.SLIME, c -> c.setNoAi(true));
			slime.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.5);
			slime.doHurtTarget(player.level(), player);
		});

		context.waitFor(_ -> controller.state().hasRumble(), 10);
	}

	private void cleanupTest(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestSingleplayerContext world
	) {
		// Wait for rumble to stop
		context.waitFor(_ -> !controller.state().hasRumble());

		world.getServer().runCommand("kill @e[type=!minecraft:player]");
	}

	private void runTest(
		ClientGameTestContext context,
		ControlifyGameTestContext controlify,
		TestControllerContext<?> controller,
		TestSingleplayerContext world,
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
		void run(ClientGameTestContext context, ControlifyGameTestContext controlify, TestControllerContext<?> controller, TestSingleplayerContext world);
	}
}
