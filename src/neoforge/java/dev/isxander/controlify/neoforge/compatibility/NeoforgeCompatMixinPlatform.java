package dev.isxander.controlify.neoforge.compatibility;

import dev.isxander.controlify.compatibility.CompatMixinPlatform;
import net.neoforged.fml.loading.FMLLoader;

public final class NeoforgeCompatMixinPlatform implements CompatMixinPlatform {
    @Override
    public boolean isModLoaded(String modId) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
    }
}
