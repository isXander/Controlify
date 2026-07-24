package dev.isxander.controlify.compatibility;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

public abstract class CompatMixinPlugin implements IMixinConfigPlugin {
    private static final Platform PLATFORM = loadPlatform();

    private final boolean compatEnabled;

    protected CompatMixinPlugin() {
        String modId = switch (PLATFORM.loader()) {
            case FABRIC -> this.getFabricModId();
            case NEOFORGE -> this.getNeoforgeModId();
        };
        this.compatEnabled = PLATFORM.impl().isModLoaded(modId);
    }

    private static Platform loadPlatform() {
        IMixinService mixinService = MixinService.getService();

        return switch (mixinService.getName()) {
            case "Knot/Fabric" -> new Platform(
                    Loader.FABRIC,
                    loadPlatformImpl(
                            mixinService,
                            "dev.isxander.controlify.fabric.compatibility.FabricCompatMixinPlatform"
                    )
            );
            case "FML" -> new Platform(
                    Loader.NEOFORGE,
                    loadPlatformImpl(
                            mixinService,
                            "dev.isxander.controlify.neoforge.compatibility.NeoforgeCompatMixinPlatform"
                    )
            );
            default -> throw new IllegalStateException("Unsupported Mixin service: " + mixinService.getName());
        };
    }

    private static CompatMixinPlatform loadPlatformImpl(IMixinService mixinService, String className) {
        try {
            // This must remain a dedicated mixin-time class, not one also used by the game.
            return mixinService.getClassProvider()
                    .findClass(className)
                    .asSubclass(CompatMixinPlatform.class)
                    .getConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to load Mixin compatibility platform: " + className, e);
        }
    }

    private enum Loader {
        FABRIC,
        NEOFORGE
    }

    private record Platform(Loader loader, CompatMixinPlatform impl) {}

    protected abstract String getModId();

    protected String getFabricModId() {
        return this.getModId();
    }

    protected String getNeoforgeModId() {
        return this.getModId();
    }

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
