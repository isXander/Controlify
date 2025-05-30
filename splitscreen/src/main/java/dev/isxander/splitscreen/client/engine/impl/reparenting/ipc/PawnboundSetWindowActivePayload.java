package dev.isxander.splitscreen.client.engine.impl.reparenting.ipc;

import dev.isxander.splitscreen.util.CSUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PawnboundSetWindowActivePayload(boolean active) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PawnboundSetWindowActivePayload> CODEC =
            ByteBufCodecs.BOOL.map(PawnboundSetWindowActivePayload::new, PawnboundSetWindowActivePayload::active).mapStream(FriendlyByteBuf::new);
    public static final Type<PawnboundSetWindowActivePayload> TYPE = new Type<>(CSUtil.rl("set_window_active"));

    @Override
    public Type<PawnboundSetWindowActivePayload> type() {
        return TYPE;
    }
}
