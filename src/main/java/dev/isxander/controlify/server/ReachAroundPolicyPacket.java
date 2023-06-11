package dev.isxander.controlify.server;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ReachAroundPolicyPacket(boolean allowed) implements FabricPacket {
    public static final PacketType<ReachAroundPolicyPacket> TYPE = PacketType.create(new ResourceLocation("controlify", "reach_around_policy"), ReachAroundPolicyPacket::new);

    public ReachAroundPolicyPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(allowed);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
