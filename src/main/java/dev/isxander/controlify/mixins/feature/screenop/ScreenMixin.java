package dev.isxander.controlify.mixins.feature.screenop;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.screenop.ScreenProcessorFactory;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.ScreenProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin implements ScreenProcessorProvider {
    @Shadow
    public abstract void clearFocus();

    @Unique
    private final ScreenProcessor<? super Screen> controlify$processor = ScreenProcessorFactory.createForScreen((Screen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return controlify$processor;
    }

    @Inject(
            method = "init(II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;init()V",
                    shift = At.Shift.AFTER
            )
    )
    private void onScreenInitialInit(CallbackInfo ci) {
        // cannot use screenProcessor() because it may be overriden by registry
        ScreenProcessorProvider.provide((Screen) (Object) this).onWidgetRebuild();
    }

    @Inject(method = "rebuildWidgets", at = @At("RETURN"))
    private void onScreenInit(CallbackInfo ci) {
        // cannot use screenProcessor() because it may be overriden by registry
        ScreenProcessorProvider.provide((Screen) (Object) this).onWidgetRebuild();
    }

    // allow controller input to set initial focus
    @ModifyExpressionValue(
            method = "setInitialFocus()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/InputType;isKeyboard()Z"
            )
    )
    private boolean shouldSetInitialFocus(boolean isKeyboard) {
        return isKeyboard || Controlify.instance().currentInputMode().isController();
    }

    // setInitialFocus just sends a tab focus event,
    // so if this is ever called once per screen open, it would have caused
    // tab to happen twice and the wrong widget to be focused
    @Inject(method = "setInitialFocus()V", at = @At("HEAD"))
    private void clearFocusBeforeTabbing(CallbackInfo ci) {
        this.clearFocus();
    }
}
