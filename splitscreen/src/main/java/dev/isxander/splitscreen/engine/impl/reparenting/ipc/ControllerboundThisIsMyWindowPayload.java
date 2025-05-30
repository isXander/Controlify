package dev.isxander.splitscreen.engine.impl.reparenting.ipc;

import dev.isxander.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;
import dev.isxander.splitscreen.util.CSUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ControllerboundThisIsMyWindowPayload(
        NativeWindowHandle windowHandle
) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ControllerboundThisIsMyWindowPayload> CODEC =
            NativeWindowHandle.STREAM_CODEC.map(ControllerboundThisIsMyWindowPayload::new, ControllerboundThisIsMyWindowPayload::windowHandle);
    public static final Type<ControllerboundThisIsMyWindowPayload> TYPE = new Type<>(CSUtil.rl("this_is_my_window"));

    @Override
    public Type<ControllerboundThisIsMyWindowPayload> type() {
        return TYPE;
    }
}
