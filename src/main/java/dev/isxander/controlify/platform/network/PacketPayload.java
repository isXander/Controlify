package dev.isxander.controlify.platform.network;

public interface PacketPayload
        //? if >1.20.4 {
        extends net.minecraft.network.protocol.common.custom.CustomPacketPayload
        //?} elif fabric {
        /*extends net.fabricmc.fabric.api.networking.v1.FabricPacket
        *///?}
{
}
