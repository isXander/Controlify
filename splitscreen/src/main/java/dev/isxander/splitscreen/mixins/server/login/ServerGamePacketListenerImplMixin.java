package dev.isxander.splitscreen.mixins.server.login;

import com.mojang.authlib.GameProfile;
import dev.isxander.splitscreen.server.login.SplitscreenLoginFlowServer;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    protected abstract GameProfile playerProfile();

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        SplitscreenLoginFlowServer.onClientDisconnect(this.playerProfile(), details);
    }
}
