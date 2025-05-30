package dev.isxander.splitscreen.engine.impl.fboshare.ipc;

import dev.isxander.splitscreen.engine.impl.fboshare.b3dext.SharedRenderTarget;
import dev.isxander.splitscreen.engine.impl.fboshare.b3dext.ShareHandle;
import dev.isxander.splitscreen.util.CSUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record ControllerboundShareMemoryPayload(
        List<SharedRenderTarget> frameBuffers,
        ShareHandle semaphore
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ControllerboundShareMemoryPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.<FriendlyByteBuf, SharedRenderTarget>list().apply(SharedRenderTarget.STREAM_CODEC),
                    ControllerboundShareMemoryPayload::frameBuffers,
                    ShareHandle.CODEC,
                    ControllerboundShareMemoryPayload::semaphore,
                    ControllerboundShareMemoryPayload::new
            );
    public static final Type<ControllerboundShareMemoryPayload> TYPE = new Type<>(CSUtil.rl("share_memory"));

    @Override
    public Type<ControllerboundShareMemoryPayload> type() {
        return TYPE;
    }
}
