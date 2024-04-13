package dev.isxander.controlify.mixins.feature.chatkbheight;

import dev.isxander.controlify.Controlify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

    // the below TAIL injects inside the multiple conditional statements, so can't use HEAD, but the first target inside the inner-most if
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;getScale()D", ordinal = 0))
    /*?if >1.20.4 {*/
    private void translateRender(GuiGraphics graphics, int tickCount, int x, int y, boolean bl, CallbackInfo ci)
    /*? } else { *//*
    private void translateRender(GuiGraphics graphics, int tickCount, int x, int y, CallbackInfo ci)
    *//*?}*/
    {
        if (!(minecraft.screen instanceof ChatScreen))
            return;

        graphics.pose().pushPose();

        Controlify.instance().getCurrentController().ifPresent(controller -> {
            if (controller.genericConfig().config().chatKeyboardHeight == 0) return;
            graphics.pose().translate(0, -controller.genericConfig().config().chatKeyboardHeight * minecraft.getWindow().getGuiScaledHeight(), 0);
        });
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void finishTranslateRender(
            GuiGraphics graphics,
            int tickCount,
            int x, int y,
            /*?if >1.20.4 {*/boolean bl,/*?}*/
            CallbackInfo ci
    ) {
        if (!(minecraft.screen instanceof ChatScreen))
            return;

        graphics.pose().popPose();
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 40))
    private int modifyChatOffset(int original) {
        if (!(minecraft.screen instanceof ChatScreen))
            return original;

        float kbHeight = Controlify.instance().getCurrentController()
                .map(c -> c.genericConfig().config().chatKeyboardHeight)
                .orElse(0f);
        if (kbHeight == 0) return original;
        return 16;
    }

    @ModifyVariable(method = "screenToChatY", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double modifyScreenY(double original) {
        if (!(minecraft.screen instanceof ChatScreen))
            return original;

        float kbHeight = Controlify.instance().getCurrentController()
                .map(c -> c.genericConfig().config().chatKeyboardHeight)
                .orElse(0f);
        if (kbHeight == 0) return original;

        return original
                - kbHeight * minecraft.getWindow().getGuiScaledHeight()
                + 24;
    }
}
