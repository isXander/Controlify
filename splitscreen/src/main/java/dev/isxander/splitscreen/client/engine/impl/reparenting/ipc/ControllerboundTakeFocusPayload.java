package dev.isxander.splitscreen.client.engine.impl.reparenting.ipc;

import dev.isxander.splitscreen.util.CSUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent when the pawn has gained focus and wants the controller to override and focus on the first window.
 */
public record ControllerboundTakeFocusPayload() implements CustomPacketPayload {
    public static final ControllerboundTakeFocusPayload UNIT = new ControllerboundTakeFocusPayload();
    public static final StreamCodec<FriendlyByteBuf, ControllerboundTakeFocusPayload> STREAM_CODEC = StreamCodec.unit(UNIT);

    public static final Type<ControllerboundTakeFocusPayload> TYPE = new Type<>(CSUtil.rl("take_focus"));

    @Override
    public Type<ControllerboundTakeFocusPayload> type() {
        return TYPE;
    }
}
