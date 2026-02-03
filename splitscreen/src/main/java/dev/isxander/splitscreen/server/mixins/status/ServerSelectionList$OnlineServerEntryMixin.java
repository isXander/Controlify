package dev.isxander.splitscreen.server.mixins.status;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.server.status.ServerStatusSplitscreenExt;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class ServerSelectionList$OnlineServerEntryMixin extends ServerSelectionList.Entry {
    @Shadow @Final private ServerData serverData;

    /**
     * Render the splitscreen status icon in the server entries
     */
    @Inject(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ServerData;getIconBytes()[B"))
    private void renderSplitscreenStatusIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick, CallbackInfo ci) {
        SplitscreenBootstrapper.getController().ifPresent(controller -> {
            ServerStatusSplitscreenExt ext = ServerStatusSplitscreenExt.getExt(serverData).orElse(null);
            if (ext == null || ext.supportedProtocols().length == 0) return;

            int x = this.getContentRight() - 5 - 10 - 10 - 2;
            boolean supported = controller.getPawnCount(false) <= ext.maxSubPlayers();
            Identifier sprite = supported
                    ? ServerStatusSplitscreenExt.SPLITSCREEN_SUPPORTED_SPRITE
                    : ServerStatusSplitscreenExt.SPLITSCREEN_UNSUPPORTED_SPRITE;

            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, this.getContentY(), 10, 8);

            if (mouseX >= x && mouseX <= x + 10 && mouseY >= this.getContentY() && mouseY <= this.getContentY() + 8) {
                Component tooltip = supported
                        ? Component.translatable("controlify.splitscreen.tooltip.supported")
                        : Component.translatable("controlify.splitscreen.tooltip.unsupported", ext.maxSubPlayers() + 1);
                guiGraphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
            }
        });
    }

    /**
     * Shift the x position of the player count to make room for the splitscreen icon
     */
    @Definition(
            id = "rightPadded",
            local = @Local(ordinal = 3, type = int.class)
    )
    @Definition(
            id = "serverStatusWidth",
            local = @Local(ordinal = 4, type = int.class)
    )
    @Expression("? = @(rightPadded - serverStatusWidth - 5)")
    @ModifyExpressionValue(method = "renderContent", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int modifyPlayerCountX(int x) {
        ServerStatusSplitscreenExt ext = ServerStatusSplitscreenExt.getExt(serverData).orElse(null);
        if (ext == null || ext.supportedProtocols().length == 0) return x;

        return x - 10 - 2;
    }
}
