package dev.isxander.controlify;

import dev.isxander.controlify.platform.Environment;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.server.ControlifyServer;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
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
//?} elif neoforge {
/*import dev.isxander.controlify.gui.screen.ModConfigOpenerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod("controlify")
public class ControlifyBootstrap {
    public ControlifyBootstrap(IEventBus modBus) {
        ControlifyServer.getInstance().onInitialize();

        if (PlatformMainUtil.getEnv() == Environment.CLIENT) {
            ModLoadingContext.get().registerExtensionPoint(
                    net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                    () -> (client, parent) -> new ModConfigOpenerScreen(parent)
            );

            Controlify.instance().preInitialiseControlify();
        }
        if (PlatformMainUtil.getEnv() == Environment.SERVER) {
            ControlifyServer.getInstance().onInitializeServer();
        }
    }
}
*///?}
