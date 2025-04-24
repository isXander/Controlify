package dev.isxander.controlify.splitscreen.window.manager;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A value class holding a reference to the native system's underlying
 * handle to a window, for example, a <code>HWND</code> on Windows or a
 * <code>XID</code> on X11.
 *
 * @param handle native handle to a window
 */
// java please add value classes
public record NativeWindowHandle(long handle) {
    public static final StreamCodec<ByteBuf, NativeWindowHandle> STREAM_CODEC =
            ByteBufCodecs.LONG.map(NativeWindowHandle::new, NativeWindowHandle::handle);
}
