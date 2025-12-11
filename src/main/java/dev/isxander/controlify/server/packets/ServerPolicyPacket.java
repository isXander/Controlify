package dev.isxander.controlify.server.packets;

import dev.isxander.controlify.utils.CUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ServerPolicyPacket(String id, boolean allowed) {
    public static final Identifier CHANNEL = CUtil.rl("server_policy");

    public static final StreamCodec<FriendlyByteBuf, ServerPolicyPacket> CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeUtf(packet.id());
            buf.writeBoolean(packet.allowed());
        },
        buf -> new ServerPolicyPacket(
            buf.readUtf(),
            buf.readBoolean()
        )
    );
}
