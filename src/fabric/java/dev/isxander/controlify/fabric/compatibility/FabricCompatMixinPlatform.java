package dev.isxander.controlify.fabric.compatibility;

import dev.isxander.controlify.compatibility.CompatMixinPlatform;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricCompatMixinPlatform implements CompatMixinPlatform {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
