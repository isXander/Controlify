package dev.isxander.splitscreen.client.features.configsync;

import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.config.SplitscreenConfig;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class ConfigSyncRegistry {
    private static final Map<Identifier, Runnable> savers = new HashMap<>();

    static {
        registerSaver(SplitscreenConfig.CONFIG_ID, SplitscreenConfig.INSTANCE::loadFromFile);
    }

    public static void registerSaver(Identifier location, Runnable saver) {
        if (savers.containsKey(location)) {
            throw new IllegalArgumentException("Duplicate saver location: " + location);
        }

        savers.put(location, saver);
    }

    public static void onSave(Identifier location) {
        SplitscreenBootstrapper.getController().ifPresent(controller -> {
            controller.forEachPawn(pawn -> pawn.onConfigSave(location));
        });
    }

    private ConfigSyncRegistry() {}
}
