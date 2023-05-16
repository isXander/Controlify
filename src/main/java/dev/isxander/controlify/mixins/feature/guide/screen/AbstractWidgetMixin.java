package dev.isxander.controlify.mixins.feature.guide.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {
    @Shadow public abstract int getX();

    @Shadow public abstract int getY();

    @Shadow public abstract int getHeight();

    @Shadow public abstract Component getMessage();

    @Shadow public abstract int getWidth();

    @Shadow public abstract boolean isActive();

    @ModifyArg(method = "renderScrollingString(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;renderScrollingString(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIII)V"), index = 3)
    protected int shiftDrawSize(int x) {
        return x;
    }
}
