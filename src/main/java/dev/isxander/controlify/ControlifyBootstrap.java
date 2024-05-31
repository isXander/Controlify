package dev.isxander.controlify;

import dev.isxander.controlify.server.ControlifyServer;

//? if fabric {
/*import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

public class ControlifyBootstrap implements ClientModInitializer, ModInitializer, DedicatedServerModInitializer {
    @Override
    public void onInitializeClient() {
        Controlify.instance().preInitialiseControlify();
    }

    @Override
    public void onInitializeServer() {
        ControlifyServer.getInstance().onInitializeServer();
    }

    @Override
    public void onInitialize() {
        ControlifyServer.getInstance().onInitialize();
    }
}
*///?} elif neoforge {
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod("controlify")
public class ControlifyBootstrap {
    public ControlifyBootstrap(IEventBus modBus) {
        ControlifyServer.getInstance().onInitialize();

        if (FMLEnvironment.dist.isClient()) {
            Controlify.instance().preInitialiseControlify();
        }

        if (FMLEnvironment.dist.isDedicatedServer()) {
            ControlifyServer.getInstance().onInitializeServer();
        }
    }
}
//?}
