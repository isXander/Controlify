package dev.isxander.controlify.compatibility;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public abstract class CompatMixinPlugin implements IMixinConfigPlugin {
    private final boolean compatEnabled;

    protected CompatMixinPlugin() {
        //? if fabric {
        /*this.compatEnabled = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(this.getModId());
        *///?} elif neoforge {
        this.compatEnabled = net.neoforged.fml.loading.LoadingModList.get().getModFileById(this.getModId()) != null;
        //?}
    }

    public abstract String getModId();

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return compatEnabled;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }
}
