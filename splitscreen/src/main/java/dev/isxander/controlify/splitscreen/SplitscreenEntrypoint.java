package dev.isxander.controlify.splitscreen;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.config.SplitscreenServerConfig;
import dev.isxander.controlify.splitscreen.server.login.SplitscreenLoginFlowServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

public class SplitscreenEntrypoint implements ModInitializer, ClientModInitializer, DedicatedServerModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean hasRanMain = false;
    private static boolean hasRanClient = false;
    private static boolean hasRanServer = false;

    @Override
    public void onInitialize() {
        hasRanMain = true;
        for (int i = 0; i < 10; i++) {
            LOGGER.info("CONTROLIFY SPLITSCREEN CLOSED BETA - DO NOT REDISTRIBUTE!!!");
        }

        SplitscreenLoginFlowServer.init();
    }

    @Override
    public void onInitializeClient() {
        hasRanClient = true;
        SplitscreenBootstrapper.bootstrap(Minecraft.getInstance());
    }

    @Override
    public void onInitializeServer() {
        hasRanServer = true;

        SplitscreenServerConfig.INSTANCE.loadFromFile();
    }

    public static boolean hasRanClient() {
        return hasRanClient;
    }
}
