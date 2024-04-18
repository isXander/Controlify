package dev.isxander.controlify.mixins.test;

import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addTestString(CallbackInfo ci) {
        this.addRenderableWidget(new PlainTextButton(10, 10, 200, 20, Component.translatable("Press %s to walk forward", BindingFontHelper.binding(new ResourceLocation("controlify", "jump"))), btn -> {}, minecraft.font));
    }
}
