package dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play;

import dev.isxander.controlify.splitscreen.remote.ipc.PawnPlayPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record PawnboundLoadConfigPacket(ResourceLocation config) implements PawnboundPlayPacket {

    public static final StreamCodec<FriendlyByteBuf, PawnboundLoadConfigPacket> CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            PawnboundLoadConfigPacket::config,
            PawnboundLoadConfigPacket::new
    );
    public static final PacketType<PawnboundLoadConfigPacket> TYPE = PawnboundPlayPacket.createType("load_config");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleLoadConfig(this);
    }

    @Override
    public @NotNull PacketType<PawnboundLoadConfigPacket> type() {
        return TYPE;
    }
}
