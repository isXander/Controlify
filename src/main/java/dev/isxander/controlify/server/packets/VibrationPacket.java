package dev.isxander.controlify.server.packets;

import dev.isxander.controlify.haptics.rumble.PatternedRumbleEffect;
import dev.isxander.controlify.haptics.rumble.RumbleEffect;
import dev.isxander.controlify.haptics.HapticSource;
import dev.isxander.controlify.haptics.rumble.RumbleState;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record VibrationPacket(HapticSource source, RumbleState[] frames) {
    public static final Identifier CHANNEL = CUtil.rl("vibration");

    public static final StreamCodec<FriendlyByteBuf, VibrationPacket> CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeIdentifier(packet.source().id());
            buf.writeInt(packet.frames().length);
            for (RumbleState frame : packet.frames()) {
                buf.writeInt(RumbleState.packToInt(frame));
            }
        },
        buf -> {
            HapticSource source = HapticSource.get(buf.readIdentifier());
            RumbleState[] frames = new RumbleState[buf.readInt()];
            for (int i = 0; i < frames.length; i++) {
                frames[i] = RumbleState.unpackFromInt(buf.readInt());
            }
            return new VibrationPacket(source, frames);
        }
    );

    public RumbleEffect createEffect() {
        return new PatternedRumbleEffect(frames).earlyFinish(() -> Minecraft.getInstance().level == null);
    }
}
