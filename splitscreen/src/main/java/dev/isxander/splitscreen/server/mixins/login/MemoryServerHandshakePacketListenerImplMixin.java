package dev.isxander.splitscreen.server.mixins.login;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.Connection;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.MemoryServerHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MemoryServerHandshakePacketListenerImpl.class)
public class MemoryServerHandshakePacketListenerImplMixin {

    /**
     * A race condition occurs within {@link net.minecraft.server.network.ServerLoginPacketListenerImpl#handleHello(ServerboundHelloPacket)}
     * where if you send outbound packets too early, it will crash with no outbound protocol configured. This change sets the outbound protocol
     * *before* the inbound protocol is set up (as is done in non-memory connections) to prevent this.
     * @param connection the connection to configure
     * @param protocol the original protocol info
     * @param packetListener the original packet listener
     * @param original the original method to call
     * @param <T> the type of packet listener
     */
    @WrapOperation(method = "handleIntention", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
    private <T extends net.minecraft.network.PacketListener> void setOutboundBeforeInbound(Connection connection, ProtocolInfo<T> protocol, T packetListener, Operation<Void> original) {
        connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
        original.call(connection, protocol, packetListener);
    }

    /**
     * Because we just set the outbound protocol, we no-op the original call to avoid doing it twice.
     * @param instance
     * @param protocol
     * @return the condition to run the original method
     */
    @WrapWithCondition(method = "handleIntention", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupOutboundProtocol(Lnet/minecraft/network/ProtocolInfo;)V"))
    private boolean shouldRunDupSetOutbound(Connection instance, ProtocolInfo<?> protocol) {
        return false;
    }
}
