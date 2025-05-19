package dev.isxander.controlify.splitscreen.mixins.server.status;

import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.server.status.ServerStatusSplitscreenExt;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public class ServerSelectionList$OnlineServerEntryMixin {
    @Shadow @Final private ServerData serverData;
    @Shadow @Final private JoinMultiplayerScreen screen;

    /**
     * Render the splitscreen status icon in the server entries
     */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ServerData;getIconBytes()[B"))
    private void renderSplitscreenStatusIcon(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick, CallbackInfo ci) {
        SplitscreenBootstrapper.getController().ifPresent(controller -> {
            ServerStatusSplitscreenExt ext = ServerStatusSplitscreenExt.getExt(serverData).orElse(null);
            if (ext == null || ext.supportedProtocols().length == 0) return;

            int x = left + width - 5 - 10 - 10 - 2;
            boolean supported = controller.getPawnCount(false) <= ext.maxSubPlayers();
            ResourceLocation sprite = supported
                    ? ServerStatusSplitscreenExt.SPLITSCREEN_SUPPORTED_SPRITE
                    : ServerStatusSplitscreenExt.SPLITSCREEN_UNSUPPORTED_SPRITE;

            guiGraphics.blitSprite(RenderType::guiTextured, sprite, x, top, 10, 8);

            if (mouseX >= x && mouseX <= x + 10 && mouseY >= top && mouseY <= top + 8) {
                Component tooltip = supported
                        ? Component.translatable("controlify.splitscreen.tooltip.supported")
                        : Component.translatable("controlify.splitscreen.tooltip.unsupported", ext.maxSubPlayers() + 1);
                screen.setTooltipForNextRenderPass(tooltip);
            }
        });
    }

    /**
     * Shift the x position of the player count to make room for the splitscreen icon
     */
    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            ),
            ordinal = 9
    )
    private int modifyPlayerCountX(int x) {
        ServerStatusSplitscreenExt ext = ServerStatusSplitscreenExt.getExt(serverData).orElse(null);
        if (ext == null || ext.supportedProtocols().length == 0) return x;

        return x - 10 - 2;
    }
}
