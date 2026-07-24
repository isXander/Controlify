package dev.isxander.controlify.compatibility;

@FunctionalInterface
public interface CompatMixinPlatform {
    boolean isModLoaded(String modId);
}
