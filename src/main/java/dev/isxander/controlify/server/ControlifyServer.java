/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.server;

import dev.isxander.controlify.platform.network.SidedNetworkApi;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.server.packets.*;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.server.level.ServerPlayer;

public class ControlifyServer {
	private static ControlifyServer INSTANCE;

	public static ControlifyServer getInstance() {
		if (INSTANCE == null) INSTANCE = new ControlifyServer();
		return INSTANCE;
	}

	public void onInitialize() {
		ControlifyHandshake.setupOnServer();

		SidedNetworkApi.S2C().registerPacket(VibrationPacket.CHANNEL, VibrationPacket.CODEC);
		SidedNetworkApi.S2C().registerPacket(OriginVibrationPacket.CHANNEL, OriginVibrationPacket.CODEC);
		SidedNetworkApi.S2C().registerPacket(EntityVibrationPacket.CHANNEL, EntityVibrationPacket.CODEC);
		SidedNetworkApi.S2C().registerPacket(ServerPolicyPacket.CHANNEL, ServerPolicyPacket.CODEC);

		PlatformMainUtil.registerCommandRegistrationCallback((dispatcher, registry, env) -> {
			VibrateCommand.register(dispatcher);
		});
	}

	public void onInitializeServer() {
		ControlifyServerConfig.HANDLER.load();
		ControlifyServerConfig.HANDLER.save();

		CUtil.LOGGER.log("Reach-around policy: {}", ControlifyServerConfig.HANDLER.instance().reachAroundPolicy);
		CUtil.LOGGER.log("No-fly drift policy: {}", ControlifyServerConfig.HANDLER.instance().noFlyDriftPolicy);
		CUtil.LOGGER.log("Allow analogue movement: {}", ControlifyServerConfig.HANDLER.instance().allowAnalogueMovement);

		ControlifyServerConfig config = ControlifyServerConfig.HANDLER.instance();
		PlatformMainUtil.registerPlayerJoinedEvent(player -> {
			setServerPolicy(ServerPolicies.REACH_AROUND, player, config.reachAroundPolicy);
			setServerPolicy(ServerPolicies.DISABLE_FLY_DRIFTING, player, config.noFlyDriftPolicy);
			setServerPolicy(ServerPolicies.ANALOGUE_MOVEMENT, player, config.allowAnalogueMovement);
		});
	}

	private void setServerPolicy(ServerPolicies policy, ServerPlayer player, boolean option) {
		// Movement must always be explicit so clients can distinguish Controlify servers
		// from servers where keyboard-like movement is the compatibility default.
		if (policy != ServerPolicies.ANALOGUE_MOVEMENT && option == policy.getUnsetValue()) return;

		SidedNetworkApi.S2C().sendPacket(
				player,
				ServerPolicyPacket.CHANNEL,
				new ServerPolicyPacket(
						policy.getId(),
						option
				)
		);
	}
}
