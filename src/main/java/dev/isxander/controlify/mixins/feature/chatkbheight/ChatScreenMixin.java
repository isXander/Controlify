package dev.isxander.controlify.mixins.feature.chatkbheight;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void translateRender(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        graphics.pose().pushPose();

        Controller<?, ?> controller = Controlify.instance().currentController();
        graphics.pose().translate(0, -controller.config().chatKeyboardHeight * this.height, 0);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void finishTranslateRender(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        graphics.pose().popPose();
    }

    @ModifyVariable(method = "mouseClicked", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double modifyClickY(double original) {
        Controller<?, ?> controller = Controlify.instance().currentController();
        return original + controller.config().chatKeyboardHeight * this.height;
    }
}
