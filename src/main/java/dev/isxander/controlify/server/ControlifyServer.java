package dev.isxander.controlify.server;

import dev.isxander.controlify.server.packets.EntityVibrationPacket;
import dev.isxander.controlify.server.packets.OriginVibrationPacket;
import dev.isxander.controlify.server.packets.ServerPolicyPacket;
import dev.isxander.controlify.server.packets.VibrationPacket;
import dev.isxander.controlify.sound.ControlifySounds;
import dev.isxander.controlify.utils.CUtil;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/*? if >1.20.4 {*/
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
/*?}*/

public class ControlifyServer implements ModInitializer, DedicatedServerModInitializer {
    @Override
    public void onInitialize() {
        ControlifySounds.init();

        /*? if >1.20.4 {*/
        PayloadTypeRegistry.playS2C().register(VibrationPacket.TYPE, VibrationPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ServerPolicyPacket.TYPE, ServerPolicyPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(EntityVibrationPacket.TYPE, EntityVibrationPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OriginVibrationPacket.TYPE, OriginVibrationPacket.CODEC);
        /*?}*/

        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> {
            VibrateCommand.register(dispatcher);
        });
    }

    @Override
    public void onInitializeServer() {
        ControlifyServerConfig.INSTANCE.load();
        ControlifyServerConfig.INSTANCE.save();

        CUtil.LOGGER.info("Reach-around policy: {}", ControlifyServerConfig.INSTANCE.getConfig().reachAroundPolicy);
        CUtil.LOGGER.info("No-fly drift policy: {}", ControlifyServerConfig.INSTANCE.getConfig().noFlyDriftPolicy);

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.send(handler.getPlayer(), new ServerPolicyPacket(ServerPolicies.REACH_AROUND.getId(), ControlifyServerConfig.INSTANCE.getConfig().reachAroundPolicy));
            ServerPlayNetworking.send(handler.getPlayer(), new ServerPolicyPacket(ServerPolicies.DISABLE_FLY_DRIFTING.getId(), ControlifyServerConfig.INSTANCE.getConfig().noFlyDriftPolicy));
        });
    }
}
