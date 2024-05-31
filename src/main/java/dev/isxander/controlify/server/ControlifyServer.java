package dev.isxander.controlify.server;

import dev.isxander.controlify.platform.network.SidedNetworkApi;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.server.packets.*;
import dev.isxander.controlify.sound.ControlifySounds;
import dev.isxander.controlify.utils.CUtil;

public class ControlifyServer {
    private static ControlifyServer INSTANCE;

    public static ControlifyServer getInstance() {
        if (INSTANCE == null) INSTANCE = new ControlifyServer();
        return INSTANCE;
    }

    public void onInitialize() {
        ControlifySounds.init();

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

        CUtil.LOGGER.info("Reach-around policy: {}", ControlifyServerConfig.HANDLER.instance().reachAroundPolicy);
        CUtil.LOGGER.info("No-fly drift policy: {}", ControlifyServerConfig.HANDLER.instance().noFlyDriftPolicy);

        PlatformMainUtil.registerPlayerJoinedEvent(player -> {
            SidedNetworkApi.S2C().sendPacket(
                    player,
                    ServerPolicyPacket.CHANNEL,
                    new ServerPolicyPacket(
                            ServerPolicies.REACH_AROUND.getId(),
                            ControlifyServerConfig.HANDLER.instance().reachAroundPolicy
                    )
            );
            SidedNetworkApi.S2C().sendPacket(
                    player,
                    ServerPolicyPacket.CHANNEL,
                    new ServerPolicyPacket(
                            ServerPolicies.DISABLE_FLY_DRIFTING.getId(),
                            ControlifyServerConfig.HANDLER.instance().noFlyDriftPolicy
                    )
            );
        });
    }
}
