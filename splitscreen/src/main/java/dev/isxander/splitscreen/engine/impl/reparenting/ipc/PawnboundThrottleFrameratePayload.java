package dev.isxander.splitscreen.engine.impl.reparenting.ipc;

import dev.isxander.splitscreen.util.CSUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PawnboundThrottleFrameratePayload(boolean throttle) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PawnboundThrottleFrameratePayload> CODEC =
            ByteBufCodecs.BOOL.map(PawnboundThrottleFrameratePayload::new, PawnboundThrottleFrameratePayload::throttle).mapStream(FriendlyByteBuf::new);
    public static final Type<PawnboundThrottleFrameratePayload> TYPE = new Type<>(CSUtil.rl("throttle_framerate"));

    @Override
    public Type<PawnboundThrottleFrameratePayload> type() {
        return TYPE;
    }
}
