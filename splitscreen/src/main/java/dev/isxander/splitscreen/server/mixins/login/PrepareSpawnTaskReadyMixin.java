package dev.isxander.splitscreen.server.mixins.login;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.splitscreen.server.SplitscreenPlayerInfo;
import dev.isxander.splitscreen.server.login.SplitscreenLoginFlowServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.server.network.config.PrepareSpawnTask$Ready")
public class PrepareSpawnTaskReadyMixin {

    @Shadow
    @Final
    private ServerLevel spawnLevel;

    /**
     * Attach the splitscreen player info when it is created. This is so you
     * can access necessary splitscreen info for the whole lifecycle of the game, not just login.
     * @param player the player that was created
     * @return the player that was created
     */
    @ModifyExpressionValue(method = "spawn", at = @At(value = "NEW", target = "Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer attachSplitscreenInfoAtLogin(ServerPlayer player) {
        var gameProfile = player.getGameProfile();
        MinecraftServer server = this.spawnLevel.getServer();

        @Nullable SplitscreenLoginFlowServer.ControllerState state = SplitscreenLoginFlowServer.getStateFromControllerOrSubplayer(gameProfile.id());

        if (state != null) {
            var holder = (SplitscreenPlayerInfo.SplitscreenPlayerInfoHolder) player;

            if (state.hostProfile().equals(gameProfile)) {
                // we are the controller
                holder.splitscreen$setPlayerInfo(new SplitscreenPlayerInfo.Controller(state.subPlayerProfiles(), state.sharedConfig(), server, player));
            } else {
                // we are a subplayer
                int subPlayerIndex = state.getSubPlayerIndex(player.getGameProfile());

                if (subPlayerIndex == -1) {
                    throw new IllegalStateException("Player " + player.getGameProfile().name() + " has no subplayer index");
                }

                holder.splitscreen$setPlayerInfo(new SplitscreenPlayerInfo.SubPlayer(state.hostProfile(), state.sharedConfig(), subPlayerIndex, server, player));
            }
        }

        return player;
    }
}
