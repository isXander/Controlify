package dev.isxander.controlify.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ServerPolicyPacket(String id, boolean allowed) implements FabricPacket {
    public static final PacketType<ServerPolicyPacket> TYPE =
            PacketType.create(
                    new ResourceLocation("controlify", "server_policy"),
                    ServerPolicyPacket::new
            );

    public ServerPolicyPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeBoolean(allowed);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
