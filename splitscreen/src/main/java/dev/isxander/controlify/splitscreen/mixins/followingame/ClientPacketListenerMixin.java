package dev.isxander.controlify.splitscreen.mixins.followingame;

import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.host.util.LANUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    @Shadow
    @Final
    private static Logger LOGGER;

    protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    /**
     * When joining a singleplayer world, immediately open to LAN
     * and connect all pawns to it.
     * @implNote opening to lan in this case only binds to 127.0.0.1 instead of 0.0.0.0 to maintain security.
     */
    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void forceSplitscreenToJoin(CallbackInfo ci) {
        SplitscreenBootstrapper.getController().ifPresent(controller -> {
            byte @Nullable [] nonce = controller.getLocalPawn().getLastNonce();
            if (this.minecraft.hasSingleplayerServer()) {
                if (nonce == null) {
                    LOGGER.error("Nonce has not been set, join attempt will probably fail.");
                }

                IntegratedServer server = this.minecraft.getSingleplayerServer();
                ServerAddress address = LANUtil.getOrPublishLANServer(server);
                controller.forEachPawn(pawn -> pawn.joinServer(address.getHost(), address.getPort(), nonce));
            } else if (nonce == null) {
                // most likely a server that does not support authenticated splitscreen.
                // TODO: implement microsoft account support.
            }
        });
    }
}
