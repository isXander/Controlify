package dev.isxander.controlify.mixins.test;

import dev.isxander.controlify.screenkeyboard.KeyboardScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addKeyboardTest(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.literal("Keyboard"), btn -> minecraft.setScreen(new KeyboardScreen((Screen) (Object) this))).build());
    }
}
