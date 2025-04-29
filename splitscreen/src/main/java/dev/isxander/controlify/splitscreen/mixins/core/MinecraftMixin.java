package dev.isxander.controlify.splitscreen.mixins.core;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ReparentingHostSplitscreenEngine;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.ReparentingRemoteSplitscreenEngine;
import dev.isxander.controlify.splitscreen.host.SplitscreenController;
import dev.isxander.controlify.splitscreen.screenop.PawnSplitscreenModeRegistry;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.parent.ParentWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.server.IntegratedServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/VirtualScreen;newWindow(Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/blaze3d/platform/Window;"))
    private DisplayData captureDisplayData(DisplayData displayData, @Share("screenSize") LocalRef<DisplayData> screenSize) {
        screenSize.set(displayData);
        return displayData;
    }

    /**
     * Hooks in and sets the splitscreen controller's window ready state.
     * <p>
     * This is conditional on the resizeDisplay because markWindowReady will cause resizeDisplay to be called anyway.
     * Allow vanilla call to continue if splitscreen controller is not present.
     * @param instance receiver
     * @return if vanilla should call resizeDisplay
     */
    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeDisplay()V"))
    private boolean markWindowReady(Minecraft instance, @Share("screenSize") LocalRef<DisplayData> screenSize) {
        SplitscreenBootstrapper.getPawn().flatMap(ReparentingRemoteSplitscreenEngine::tryGet).ifPresent(ReparentingRemoteSplitscreenEngine::onWindowInit);

        return SplitscreenBootstrapper.getController().flatMap(ReparentingHostSplitscreenEngine::tryGet).map(engine -> {
            engine.initWindow(screenSize.get(), ((VirtualScreenAccessor) instance).getScreenManager(), this.createTitle());
            return false;
        }).orElse(true);
    }

    /**
     * Ensures the parent window is closed when the game exist.
     */
    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;close()V"))
    private void closeParentWindow(CallbackInfo ci) {
        SplitscreenBootstrapper.getController()
                .flatMap(ReparentingHostSplitscreenEngine::tryGet)
                .ifPresent(engine -> engine.getParentWindow().close());
    }


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
        int pawnCount = controller.getPawnCount();
        boolean localOnlyLanServer = playerCount == pawnCount;

        boolean isFullscreen = PawnSplitscreenModeRegistry.getMode(this.screen) == ScreenSplitscreenMode.FULLSCREEN;

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
