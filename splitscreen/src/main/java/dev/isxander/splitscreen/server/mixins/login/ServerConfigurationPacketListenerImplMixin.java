package dev.isxander.splitscreen.server.mixins.login;

import com.mojang.authlib.GameProfile;
import dev.isxander.splitscreen.server.login.SplitscreenLoginFlowServer;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public class ServerConfigurationPacketListenerImplMixin {
    @Shadow
    @Final
    private GameProfile gameProfile;

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        SplitscreenLoginFlowServer.onClientDisconnect(this.gameProfile, details);
    }
}
