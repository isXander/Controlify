package dev.isxander.controlify.splitscreen.mixins.followingame;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.net.InetAddress;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {
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

        // TODO: make this a setting to allow regular LAN play
        return InetAddress.getLoopbackAddress();
    }
}
