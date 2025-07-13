package dev.isxander.splitscreen.server.mixins.login;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.splitscreen.server.SplitscreenPlayerInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {

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
