package dev.isxander.controlify.mixins.feature.chatkbheight;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
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
    private void translateRender(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        matrices.pushPose();

        Controller<?, ?> controller = Controlify.instance().currentController();
        matrices.translate(0, -controller.config().chatKeyboardHeight * this.height, 0);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void finishTranslateRender(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        matrices.popPose();
    }

    @ModifyVariable(method = "mouseClicked", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double modifyClickY(double original) {
        Controller<?, ?> controller = Controlify.instance().currentController();
        return original + controller.config().chatKeyboardHeight * this.height;
    }
}
