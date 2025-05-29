package dev.isxander.controlify.splitscreen.mixins.screenop;

import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.host.gui.SplitscreenConfigGuiFactory;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {
    @Shadow
    protected abstract Button openScreenButton(Component name, Supplier<Screen> screenSupplier);

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE:LAST",
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"
            )
    )
    private void addSplitscreenConfig(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        if (SplitscreenBootstrapper.getController().isPresent() || !SplitscreenBootstrapper.isSplitscreen()) {
            rowHelper.addChild(this.openScreenButton(Component.translatable("controlify.splitscreen.open_button"), () -> SplitscreenConfigGuiFactory.buildScreen((OptionsScreen) (Object) this)));
        }
    }
}
