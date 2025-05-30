package dev.isxander.splitscreen.server.mixins.login;

import dev.isxander.splitscreen.server.SplitscreenPlayerInfo;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements SplitscreenPlayerInfo.SplitscreenPlayerInfoHolder {
    @Unique
    private @Nullable SplitscreenPlayerInfo splitscreen$playerInfo;

    @Override
    public @Nullable SplitscreenPlayerInfo splitscreen$getPlayerInfo() {
        return splitscreen$playerInfo;
    }

    @Override
    public void splitscreen$setPlayerInfo(SplitscreenPlayerInfo playerInfo) {
        this.splitscreen$playerInfo = playerInfo;
    }
}
