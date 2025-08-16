package dev.isxander.controlify.mixins.feature.screenop.impl.elements;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractWidget.class)
public class AbstractWidgetMixin {
    @WrapWithCondition(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    //? if >=1.21.9 {
                    target = "Lnet/minecraft/client/gui/components/AbstractWidget;onClick(DDZ)V"
                    //?} else {
                    /*target = "Lnet/minecraft/client/gui/components/AbstractWidget;onClick(DD)V"
                    *///?}
            )
    )
    private boolean openKeyboardInVmouseMode(AbstractWidget instance, double x, double y /*? if >=1.21.9 {*/,boolean b/*?}*/) {
        Controlify controlify = Controlify.instance();
        ControllerEntity controller = controlify.getCurrentController().orElse(null);

        if (controller != null && controlify.virtualMouseHandler().isVirtualMouseEnabled()) {
            var screenProcessor = ScreenProcessorProvider.provide(Minecraft.getInstance().screen);
            return !screenProcessor.tryOpenKeyboard(controller, (AbstractWidget) (Object) this);
        }

        return true;
    }
}
