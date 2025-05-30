package dev.isxander.splitscreen.mixins.core;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.splitscreen.SplitscreenBootstrapper;
import dev.isxander.splitscreen.host.SplitscreenController;
import dev.isxander.splitscreen.screenop.PawnSplitscreenModeRegistry;
import dev.isxander.splitscreen.screenop.ScreenSplitscreenMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.server.IntegratedServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public Screen screen;

    @Shadow
    @Nullable
    private IntegratedServer singleplayerServer;

    @Shadow
    protected abstract String createTitle();

    @Shadow
    @Final
    private VirtualScreen virtualScreen;

    /**
     * Minecraft only usually pauses the game when in a singleplayer world with no LAN.
     * This is a problem for splitscreen, even if all players are local, it will be treated as
     * a multiplayer world and continue to tick the server in the background.
     * Whilst this is the correct behaviour when a single pawn is paused, in fullscreen screens where
     * no games are visible, this should pause the game.
     * @param isLANServer if the integrated server is open to LAN
     * @return whether the server should NOT be paused (aka if the server should tick)
     */
    @Definition(id = "pause", field = "Lnet/minecraft/client/Minecraft;pause:Z")
    @Definition(id = "localServer", field = "Lnet/minecraft/client/Minecraft;singleplayerServer:Lnet/minecraft/client/server/IntegratedServer;")
    @Definition(id = "isPublished", method = "Lnet/minecraft/client/server/IntegratedServer;isPublished()Z")
    @Expression(value = "this.pause = ?", id = "slice_end")
    @Expression(value = "this.localServer.isPublished()", id = "target")
    @ModifyExpressionValue(
            method = "runTick",
            slice = @Slice(to = @At(value = "MIXINEXTRAS:EXPRESSION", id = "slice_end")),
            at = @At(value = "MIXINEXTRAS:EXPRESSION:LAST", id = "target")
    )
    private boolean shouldTickServerInPausableScreen(boolean isLANServer) {
        if (!isLANServer) return false;

        Optional<SplitscreenController> controllerOpt = SplitscreenBootstrapper.getController();
        if (controllerOpt.isEmpty()) return true;
        SplitscreenController controller = controllerOpt.get();

        int playerCount = this.singleplayerServer.getPlayerCount();
        int pawnCount = controller.getPawnCount(true);
        boolean localOnlyLanServer = playerCount == pawnCount;

        // even if this screen isn't a fullscreen screen, if there's only one player all screens are fullscreen
        boolean isFullscreen = PawnSplitscreenModeRegistry.getMode(this.screen) == ScreenSplitscreenMode.FULLSCREEN
                || playerCount == 1;

        return localOnlyLanServer && !isFullscreen;
    }

    /**
     * Inject when game is fully ready to notify the controller that we are ready and
     * it can remove the waiting screen.
     */
    @Inject(method = "onGameLoadFinished", at = @At("HEAD"))
    private void notifyControllerGameReady(CallbackInfo ci) {
        SplitscreenBootstrapper.getControllerBridge().ifPresent(bridge -> bridge.signalImReady(true, 1));
    }

    /**
     * Controlify Splitscreen overrides splitscreen modes when an overlay is present
     * to fullscreen only. This ensures that we don't go out of sync with that.
     */
    @Inject(method = "setOverlay", at = @At("RETURN"))
    private void updateSplitscreenWhenOverlayChanges(CallbackInfo ci) {
        SplitscreenBootstrapper.getController().ifPresent(SplitscreenController::updateSplitscreenMode);
    }
}
