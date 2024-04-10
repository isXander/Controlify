package dev.isxander.controlify.server.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/*? if >1.20.4 {*/
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/*? } else {*//*
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
*//*? }*/

public record ServerPolicyPacket(String id, boolean allowed)
        /*? if >1.20.4 {*/
        implements CustomPacketPayload
        /*? } else {*//*
        implements FabricPacket
        *//*? }*/
{
    private static final ResourceLocation ID = new ResourceLocation("controlify", "server_policy");

    /*? if >1.20.4 {*/
    public static final CustomPacketPayload.Type<ServerPolicyPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerPolicyPacket> CODEC = StreamCodec.ofMember(ServerPolicyPacket::write, ServerPolicyPacket::new);
    /*? } else {*//*
    public static final PacketType<ServerPolicyPacket> TYPE = PacketType.create(ID, ServerPolicyPacket::new);
    *//*? }*/

    public ServerPolicyPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readBoolean());
    }

    /*? if <=1.20.4 {*//*
    @Override
    *//*? }*/
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeBoolean(allowed);
    }

    /*? if >1.20.4 {*/
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    /*? } else {*//*
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
    *//*? }*/
}
