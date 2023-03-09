package dev.isxander.controlify.mixins.feature.chatkbheight;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "render", at = @At("HEAD"))
    private void translateRender(PoseStack matrices, int tickDelta, int i, int j, CallbackInfo ci) {
        if (!(minecraft.screen instanceof ChatScreen))
            return;

        Controller<?, ?> controller = Controlify.instance().currentController();
        matrices.pushPose();
        if (controller.config().chatKeyboardHeight == 0) return;
        matrices.translate(0, -controller.config().chatKeyboardHeight * minecraft.getWindow().getGuiScaledHeight(), 0);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void finishTranslateRender(PoseStack matrices, int tickDelta, int i, int j, CallbackInfo ci) {
        if (!(minecraft.screen instanceof ChatScreen))
            return;

        matrices.popPose();
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 40))
    private int modifyChatOffset(int original) {
        if (!(minecraft.screen instanceof ChatScreen))
            return original;

        Controller<?, ?> controller = Controlify.instance().currentController();
        if (controller.config().chatKeyboardHeight == 0) return original;
        return 16;
    }

    @ModifyVariable(method = "screenToChatY", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double modifyScreenY(double original) {
        if (!(minecraft.screen instanceof ChatScreen))
            return original;

        Controller<?, ?> controller = Controlify.instance().currentController();
        if (controller.config().chatKeyboardHeight == 0) return original;

        return original
                - controller.config().chatKeyboardHeight * minecraft.getWindow().getGuiScaledHeight()
                + 24;
    }
}
