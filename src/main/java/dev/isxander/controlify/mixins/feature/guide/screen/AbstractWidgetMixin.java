package dev.isxander.controlify.mixins.feature.guide.screen;

import dev.isxander.controlify.gui.DrawSize;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin extends GuiComponent {
    @Shadow public abstract int getX();

    @Shadow public abstract int getY();

    @Shadow public abstract int getHeight();

    @Shadow public abstract Component getMessage();

    @Shadow public abstract int getWidth();

    @Shadow public abstract boolean isActive();

    @ModifyArg(method = "renderScrollingString(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;renderScrollingString(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIII)V"), index = 3)
    protected int shiftDrawSize(int x) {
        return x;
    }
}
