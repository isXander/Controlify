package dev.isxander.controlify.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    public static final Logger LOGGER = LoggerFactory.getLogger("Controlify");

    public static final Version VERSION = FabricLoader.getInstance().getModContainer("controlify").orElseThrow().getMetadata().getVersion();
}
