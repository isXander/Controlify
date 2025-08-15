package dev.isxander.controlify.mixins.feature.patches.bookfocusfix;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.isxander.controlify.Controlify;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BookEditScreen.class)
public class BookEditScreenMixin {
    // 1.21.6 no longer forces focus every frame
    //? if <1.21.6 {
    /*/^*
     * The book screen removes focus every frame, which causes the ScreenProcessor to fight it and cause flickering.
     * This mixin prevents that from happening by only removing focus when not using a controller.
     ^/
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;setFocused(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V"))
    private boolean shouldRemoveFocus(BookEditScreen instance, GuiEventListener guiEventListener) {
        return !Controlify.instance().currentInputMode().isController();
    }
    *///?}
}
