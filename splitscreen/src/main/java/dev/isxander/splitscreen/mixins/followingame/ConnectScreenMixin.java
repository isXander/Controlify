package dev.isxander.splitscreen.mixins.followingame;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.isxander.splitscreen.SplitscreenBootstrapper;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    /**
     * Do not allow remote pawns to disconnect from the servers they are forced into by the host.
     * @param instance receiver (this screen)
     * @param guiEventListener the cancel button
     * @return if the cancel button should be added
     */
    @Definition(id = "addRenderableWidget", method = "Lnet/minecraft/client/gui/screens/ConnectScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;")
    @Definition(id = "buttonBuilder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
    @Definition(id = "GUI_CANCEL", field = "Lnet/minecraft/network/chat/CommonComponents;GUI_CANCEL:Lnet/minecraft/network/chat/Component;")
    @Expression("this.addRenderableWidget(buttonBuilder(GUI_CANCEL, ?).?(?, ?, ?, ?).?())")
    @WrapWithCondition(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean shouldAddCancelButton(ConnectScreen instance, GuiEventListener guiEventListener) {
        return SplitscreenBootstrapper.getPawn().isEmpty();
    }
}
