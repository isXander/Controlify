package dev.isxander.splitscreen.client.features.configsync;

import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.config.SplitscreenConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class ConfigSyncRegistry {
    private static final Map<ResourceLocation, Runnable> savers = new HashMap<>();

    static {
        registerSaver(SplitscreenConfig.CONFIG_ID, SplitscreenConfig.INSTANCE::loadFromFile);
    }

    public static void registerSaver(ResourceLocation location, Runnable saver) {
        if (savers.containsKey(location)) {
            throw new IllegalArgumentException("Duplicate saver location: " + location);
        }

        savers.put(location, saver);
    }

    public static void onSave(ResourceLocation location) {
        SplitscreenBootstrapper.getController().ifPresent(controller -> {
            controller.forEachPawn(pawn -> pawn.onConfigSave(location));
        });
    }

    private ConfigSyncRegistry() {}
}
