package dev.isxander.splitscreen.client.mixins.followingame;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.host.util.LANUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.net.InetAddress;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * Modifies the LAN bind address to be loopback, rather than binding on the host.
     * This means opening to LAN does not make the server discoverable to other devices
     * on the network, and only the local machine can connect to it.
     * @param original the original bind address, {@code null} (which is host)
     * @return the modified bind address
     */
    @Definition(id = "startTcpServerListener", method = "Lnet/minecraft/server/network/ServerConnectionListener;startTcpServerListener(Ljava/net/InetAddress;I)V")
    @Definition(id = "port", local = @Local(type = int.class, argsOnly = true))
    @Expression("?.startTcpServerListener(null, port)")
    @ModifyArg(method = "publishServer", at = @At("MIXINEXTRAS:EXPRESSION"))
    private InetAddress modifyLANBindAddress(InetAddress original) {
        if (!SplitscreenBootstrapper.isSplitscreen()) return original;

        return LANUtil.getLANServerBindAddress();
    }


    // ----
    // Below methods are to prevent any method calls on the `null` player if publishing too early.
    // ----

    /**
     * Splitscreen can call publishServer before {@link Minecraft#getConnection()} is not null.
     * (Its impl fetches the connection from {@link Minecraft#player})
     * This mixin prevents NPE by calling what {@link ClientPacketListener#prepareKeyPair()} does.
     */
    @WrapOperation(method = "publishServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;prepareKeyPair()V"))
    private void redirectPrepareKeyPair(ClientPacketListener instance, Operation<Void> original) {
        if (instance == null) {
            this.minecraft.getProfileKeyPairManager().prepareKeyPair();
        } else {
            original.call(instance);
        }
    }

    @WrapOperation(method = "publishServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getGameProfile()Lcom/mojang/authlib/GameProfile;"))
    private GameProfile preventPlayerNPE0(LocalPlayer instance, Operation<GameProfile> original) {
        if (instance == null) return null;
        return original.call(instance);
    }
    @WrapOperation(method = "publishServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;getProfilePermissions(Lcom/mojang/authlib/GameProfile;)I"))
    private int preventPlayerNPE1(IntegratedServer instance, GameProfile gameProfile, Operation<Integer> original) {
        if (gameProfile == null) return 0;
        return original.call(instance, gameProfile);
    }
    @WrapOperation(method = "publishServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setPermissionLevel(I)V"))
    private void preventPlayerNPE2(LocalPlayer instance, int permissionLevel, Operation<Void> original) {
        if (instance == null) return;
        original.call(instance, permissionLevel);
    }
}
