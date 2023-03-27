package dev.isxander.controlify.mixins.feature.screenop;

import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin implements ScreenProcessorProvider {
    @Unique
    private final ScreenProcessor<Screen> controlify$processor = new ScreenProcessor<>((Screen) (Object) this);

    @Override
    public ScreenProcessor<Screen> screenProcessor() {
        return controlify$processor;
    }

    @Inject(method = "rebuildWidgets", at = @At("RETURN"))
    private void onScreenInit(CallbackInfo ci) {
        // cannot use screenProcessor() because it may be overriden by registry
        ScreenProcessorProvider.provide((Screen) (Object) this).onWidgetRebuild();
    }

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("HEAD"))
    private void clearRegistryCaches(Minecraft client, int width, int height, CallbackInfo ci) {
        ScreenProcessorProvider.REGISTRY.clearCache();
        ComponentProcessorProvider.REGISTRY.clearCache();
    }
}
