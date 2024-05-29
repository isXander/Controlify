package dev.isxander.controlify.server;

import dev.isxander.controlify.platform.network.SidedNetworkApi;
import dev.isxander.controlify.platform.server.PlatformServerUtil;
import dev.isxander.controlify.server.packets.*;
import dev.isxander.controlify.sound.ControlifySounds;
import dev.isxander.controlify.utils.CUtil;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

public class ControlifyServer implements ModInitializer, DedicatedServerModInitializer {
    @Override
    public void onInitialize() {
        ControlifySounds.init();

        ControlifyHandshake.setupOnServer();

        SidedNetworkApi.S2C().registerPacket(VibrationPacket.CHANNEL, VibrationPacket.CODEC);
        SidedNetworkApi.S2C().registerPacket(OriginVibrationPacket.CHANNEL, OriginVibrationPacket.CODEC);
        SidedNetworkApi.S2C().registerPacket(EntityVibrationPacket.CHANNEL, EntityVibrationPacket.CODEC);
        SidedNetworkApi.S2C().registerPacket(ServerPolicyPacket.CHANNEL, ServerPolicyPacket.CODEC);

        PlatformServerUtil.registerCommandRegistrationCallback((dispatcher, registry, env) -> {
            VibrateCommand.register(dispatcher);
        });
    }

    @Override
    public void onInitializeServer() {
        ControlifyServerConfig.HANDLER.load();
        ControlifyServerConfig.HANDLER.save();

        CUtil.LOGGER.info("Reach-around policy: {}", ControlifyServerConfig.HANDLER.instance().reachAroundPolicy);
        CUtil.LOGGER.info("No-fly drift policy: {}", ControlifyServerConfig.HANDLER.instance().noFlyDriftPolicy);

        PlatformServerUtil.registerInitPlayConnectionEvent((handler, server) -> {
            SidedNetworkApi.S2C().sendPacket(
                    handler.getPlayer(),
                    ServerPolicyPacket.CHANNEL,
                    new ServerPolicyPacket(
                            ServerPolicies.REACH_AROUND.getId(),
                            ControlifyServerConfig.HANDLER.instance().reachAroundPolicy
                    )
            );
            SidedNetworkApi.S2C().sendPacket(
                    handler.getPlayer(),
                    ServerPolicyPacket.CHANNEL,
                    new ServerPolicyPacket(
                            ServerPolicies.DISABLE_FLY_DRIFTING.getId(),
                            ControlifyServerConfig.HANDLER.instance().noFlyDriftPolicy
                    )
            );
        });
    }
}
