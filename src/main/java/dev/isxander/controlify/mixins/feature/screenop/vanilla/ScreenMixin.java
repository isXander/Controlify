package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.ScreenProcessor;
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
        screenProcessor().onWidgetRebuild();
    }
}
