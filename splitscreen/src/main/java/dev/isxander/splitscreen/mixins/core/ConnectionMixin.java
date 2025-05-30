package dev.isxander.splitscreen.mixins.core;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.splitscreen.ipc.ConnectionDisconnectPacketFactory;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Shadow
    private volatile boolean sendLoginDisconnect;

    @Definition(id = "send", method = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V")
    @Expression("this.send(@(?), ?)")
    @ModifyExpressionValue(method = "exceptionCaught", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Packet<?> modifyDisconnectPacket(Packet<?> packet, @Local Component reason, @Local(argsOnly = true) Throwable throwable) {
        if (this instanceof ConnectionDisconnectPacketFactory factory) {
            return factory.createDisconnectPacket(throwable, reason, this.sendLoginDisconnect);
        }
        return packet;
    }
}
