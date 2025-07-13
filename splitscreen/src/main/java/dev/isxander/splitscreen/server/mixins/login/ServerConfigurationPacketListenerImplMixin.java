package dev.isxander.splitscreen.server.mixins.login;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import dev.isxander.splitscreen.server.SplitscreenPlayerInfo;
import dev.isxander.splitscreen.server.login.SplitscreenLoginFlowServer;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin extends ServerCommonPacketListenerImpl {
    @Shadow
    @Final
    private GameProfile gameProfile;

    public ServerConfigurationPacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
        super(server, connection, cookie);
    }

    /**
     * Attach the splitscreen player info when it is created. This is so you
     * can access necessary splitscreen info for the whole lifecycle of the game, not just login.
     * @param player the player that was created
     * @return the player that was created
     */
    @ModifyExpressionValue(method = "handleConfigurationFinished", at = @At(value = "NEW", target = "Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer attachSplitscreenInfoAtLogin(ServerPlayer player) {
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

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        SplitscreenLoginFlowServer.onClientDisconnect(this.gameProfile, details);
    }
}
