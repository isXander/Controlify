package dev.isxander.controlify.server;

import dev.isxander.controlify.utils.Log;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ControlifyServer implements ModInitializer, DedicatedServerModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> {
            VibrateCommand.register(dispatcher);
        });
    }

    @Override
    public void onInitializeServer() {
        ControlifyServerConfig.INSTANCE.load();
        Log.LOGGER.info("Reach-around policy: " + ControlifyServerConfig.INSTANCE.getConfig().reachAroundPolicy);

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.send(handler.getPlayer(), new ReachAroundPolicyPacket(ControlifyServerConfig.INSTANCE.getConfig().reachAroundPolicy));
        });
    }
}
