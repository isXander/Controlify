package dev.isxander.controlify.api.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

final class ControlifyApiLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("Controlify API Loader");
    private static ControlifyApi INSTANCE = null;

    static ControlifyApi get() {
        if (INSTANCE == null) {
            INSTANCE = ServiceLoader.load(ControlifyApi.class)
                    .findFirst()
                    .orElseGet(() -> {
                        LOGGER.warn("Controlify API implementation not found. Controlify not installed?");
                        return null;
                    });
        }
        return INSTANCE;
    }
}
