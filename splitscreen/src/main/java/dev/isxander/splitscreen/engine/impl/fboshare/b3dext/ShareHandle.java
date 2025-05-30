package dev.isxander.splitscreen.engine.impl.fboshare.b3dext;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public sealed interface ShareHandle {
    StreamCodec<FriendlyByteBuf, ShareHandle> CODEC = StreamCodec.of(
            (buf, handle) -> {
                switch (handle) {
                    case Fd fd -> {
                        buf.writeByte(0);
                        Fd.STREAM_CODEC.encode(buf, fd);
                    }
                    case Win32 win32 -> {
                        buf.writeByte(1);
                        Win32.STREAM_CODEC.encode(buf, win32);
                    }
                }
            },
            buf -> {
                byte type = buf.readByte();
                if (type == 0) {
                    return Fd.STREAM_CODEC.decode(buf);
                } else if (type == 1) {
                    return Win32.STREAM_CODEC.decode(buf);
                } else {
                    throw new IllegalArgumentException("Unknown share handle type: " + type);
                }
            }
    );

    record Fd(int fd) implements ShareHandle {
        public static final StreamCodec<ByteBuf, Fd> STREAM_CODEC =
                ByteBufCodecs.INT.map(Fd::new, Fd::fd);
    }

    record Win32(long handle) implements ShareHandle {
        public static final StreamCodec<ByteBuf, Win32> STREAM_CODEC =
                ByteBufCodecs.LONG.map(Win32::new, Win32::handle);
    }
}
