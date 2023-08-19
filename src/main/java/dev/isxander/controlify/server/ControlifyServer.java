package dev.isxander.controlify.server;

import dev.isxander.controlify.sound.ControlifySounds;
import dev.isxander.controlify.utils.Log;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ControlifyServer implements ModInitializer, DedicatedServerModInitializer {
    @Override
    public void onInitialize() {
        ControlifySounds.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> {
            VibrateCommand.register(dispatcher);
        });
    }

    @Override
    public void onInitializeServer() {
        ControlifyServerConfig.INSTANCE.load();
        ControlifyServerConfig.INSTANCE.save();

        Log.LOGGER.info("Reach-around policy: " + ControlifyServerConfig.INSTANCE.getConfig().reachAroundPolicy);
        Log.LOGGER.info("No-fly drift policy: " + ControlifyServerConfig.INSTANCE.getConfig().noFlyDriftPolicy);

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.send(handler.getPlayer(), new ServerPolicyPacket(ServerPolicies.REACH_AROUND.getId(), ControlifyServerConfig.INSTANCE.getConfig().reachAroundPolicy));
            ServerPlayNetworking.send(handler.getPlayer(), new ServerPolicyPacket(ServerPolicies.DISABLE_FLY_DRIFTING.getId(), ControlifyServerConfig.INSTANCE.getConfig().noFlyDriftPolicy));

            // backwards compat
            ServerPlayNetworking.send(handler.getPlayer(), new LegacyReachAroundPolicyPacket(ControlifyServerConfig.INSTANCE.getConfig().reachAroundPolicy));
        });
    }
}
