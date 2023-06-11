package dev.isxander.controlify.server;

import dev.isxander.yacl3.config.ConfigInstance;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.fabricmc.loader.api.FabricLoader;

public class ControlifyServerConfig {
    public static final ConfigInstance<ControlifyServerConfig> INSTANCE = GsonConfigInstance.createBuilder(ControlifyServerConfig.class)
            .setPath(FabricLoader.getInstance().getConfigDir().resolve("controlify.json"))
            .build();

    public boolean reachAroundPolicy = false;
}
