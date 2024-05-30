package dev.isxander.controlify.server;

import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;

public class ControlifyServerConfig {
    public static final ConfigClassHandler<ControlifyServerConfig> HANDLER = ConfigClassHandler.createBuilder(ControlifyServerConfig.class)
            .id(CUtil.rl("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(PlatformMainUtil.getConfigDir().resolve("controlify.json"))
                    .build())
            .build();

    @SerialEntry public boolean reachAroundPolicy = true;
    @SerialEntry public boolean noFlyDriftPolicy = true;
}
