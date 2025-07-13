package dev.isxander.splitscreen.client.ipc.packets.pawnbound.play;

import dev.isxander.splitscreen.client.InputMethod;
import dev.isxander.splitscreen.client.remote.ipc.PawnPlayPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record PawnboundUseInputMethodPacket(InputMethod inputMethod) implements PawnboundPlayPacket {
    public static final StreamCodec<FriendlyByteBuf, PawnboundUseInputMethodPacket> CODEC =
            StreamCodec.composite(
                    InputMethod.STREAM_CODEC,
                    PawnboundUseInputMethodPacket::inputMethod,
                    PawnboundUseInputMethodPacket::new
            );
    public static final PacketType<PawnboundUseInputMethodPacket> TYPE = PawnboundPlayPacket.createType("use_input_method");

    @Override
    public void handle(PawnPlayPacketListener handler) {
        handler.handleUseInputMethod(this);
    }

    @Override
    public PacketType<PawnboundUseInputMethodPacket> type() {
        return TYPE;
    }
}
