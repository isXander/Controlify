package dev.isxander.controlify.mixins.feature.guide.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {

    @Shadow public abstract int getX();

    @Shadow public abstract int getY();

    @Shadow public abstract int getHeight();

    @Shadow public abstract int getWidth();

    @Shadow
    public abstract Component getMessage();

    @Inject(method = "setMessage", at = @At("RETURN"))
    protected void catchMessageSet(Component message, CallbackInfo ci) {
    }

    @ModifyExpressionValue(method = "renderScrollingString(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;getMessage()Lnet/minecraft/network/chat/Component;"))
    protected Component modifyRenderedMessage(Component actualMessage) {
        return actualMessage;
    }
}
