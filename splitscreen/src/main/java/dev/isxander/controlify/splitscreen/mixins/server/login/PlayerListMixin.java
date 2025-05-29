package dev.isxander.controlify.splitscreen.mixins.server.login;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import dev.isxander.controlify.splitscreen.server.SplitscreenPlayerInfo;
import dev.isxander.controlify.splitscreen.server.login.SplitscreenLoginFlowServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Shadow @Final private MinecraftServer server;

    /**
     * Attach the splitscreen player info when it is created. This is so you
     * can access necessary splitscreen info for the whole lifecycle of the game, not just login.
     * @param player the player that was created
     * @param gameProfile the game profile of the player
     * @return the player that was created
     */
    @ModifyReturnValue(method = "getPlayerForLogin", at = @At("RETURN"))
    private ServerPlayer attachSplitscreenInfoAtLogin(ServerPlayer player, @Local(argsOnly = true) GameProfile gameProfile) {
        @Nullable SplitscreenLoginFlowServer.ControllerState state = SplitscreenLoginFlowServer.getStateFromControllerOrSubplayer(gameProfile.getId());

        if (state != null) {
            var holder = (SplitscreenPlayerInfo.SplitscreenPlayerInfoHolder) player;

            if (state.hostProfile().equals(gameProfile)) {
                // we are the controller
                holder.splitscreen$setPlayerInfo(new SplitscreenPlayerInfo.Controller(state.subPlayerProfiles(), state.sharedConfig(), this.server, player));
            } else {
                // we are a subplayer
                int subPlayerIndex = state.getSubPlayerIndex(player.getGameProfile());

                if (subPlayerIndex == -1) {
                    throw new IllegalStateException("Player " + player.getGameProfile().getName() + " has no subplayer index");
                }

                holder.splitscreen$setPlayerInfo(new SplitscreenPlayerInfo.SubPlayer(state.hostProfile(), state.sharedConfig(), subPlayerIndex, this.server, player));
            }
        }

        return player;
    }

    /**
     * Copy the splitscreen player info from the old player to the new player at a respawn.
     * @param player the new player being created
     * @param oldPlayer the old, dead, player
     * @return the new player
     */
    @ModifyExpressionValue(method = "respawn", at = @At(value = "NEW", target = "net/minecraft/server/level/ServerPlayer"))
    private ServerPlayer attachSplitscreenInfoAtRespawn(ServerPlayer player, @Local(argsOnly = true) ServerPlayer oldPlayer) {
        ((SplitscreenPlayerInfo.SplitscreenPlayerInfoHolder) player).splitscreen$setPlayerInfo(SplitscreenPlayerInfo.get(oldPlayer).orElse(null));
        return player;
    }
}
