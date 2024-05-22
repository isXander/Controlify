package dev.isxander.controlify.server.packets;

import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public record VibrationPacket(RumbleSource source, RumbleState[] frames) {
    public static final ResourceLocation CHANNEL = CUtil.rl("vibration");

    public static final ControlifyPacketCodec<VibrationPacket> CODEC = ControlifyPacketCodec.of(
        (buf, packet) -> {
            buf.writeResourceLocation(packet.source().id());
            buf.writeInt(packet.frames().length);
            for (RumbleState frame : packet.frames()) {
                buf.writeInt(RumbleState.packToInt(frame));
            }
        },
        buf -> {
            RumbleSource source = RumbleSource.get(buf.readResourceLocation());
            RumbleState[] frames = new RumbleState[buf.readInt()];
            for (int i = 0; i < frames.length; i++) {
                frames[i] = RumbleState.unpackFromInt(buf.readInt());
            }
            return new VibrationPacket(source, frames);
        }
    );

    public RumbleEffect createEffect() {
        return new BasicRumbleEffect(frames).earlyFinish(() -> Minecraft.getInstance().level == null);
    }
}
