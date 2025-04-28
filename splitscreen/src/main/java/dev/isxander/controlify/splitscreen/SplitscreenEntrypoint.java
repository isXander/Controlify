package dev.isxander.controlify.splitscreen;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

public class SplitscreenEntrypoint implements ClientModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean hasRan = false;

    @Override
    public void onInitializeClient() {
        for (int i = 0; i < 10; i++) {
            LOGGER.info("CONTROLIFY SPLITSCREEN CLOSED BETA - DO NOT REDISTRIBUTE!!!");
        }

        hasRan = true;
        SplitscreenBootstrapper.bootstrap(Minecraft.getInstance());
    }

    public static boolean hasRan() {
        return hasRan;
    }
}
