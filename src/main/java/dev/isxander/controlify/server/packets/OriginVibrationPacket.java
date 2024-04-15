package dev.isxander.controlify.server.packets;

import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public record OriginVibrationPacket(Vector3f origin, float effectRange, int duration, RumbleState state, RumbleSource source) {
    public static final ResourceLocation CHANNEL = new ResourceLocation("controlify", "vibrate_from_origin");

    public static final ControlifyPacketCodec<OriginVibrationPacket> CODEC = ControlifyPacketCodec.of(
        (buf, packet) -> {
            buf.writeVector3f(packet.origin());
            buf.writeFloat(packet.effectRange());
            buf.writeVarInt(packet.duration());
            buf.writeInt(RumbleState.packToInt(packet.state()));
            buf.writeResourceLocation(packet.source().id());
        },
        buf -> new OriginVibrationPacket(
            buf.readVector3f(),
            buf.readFloat(),
            buf.readVarInt(),
            RumbleState.unpackFromInt(buf.readInt()),
            RumbleSource.get(buf.readResourceLocation())
        )
    );

    public RumbleEffect createEffect() {
        var originVec3 = new Vec3(origin);
        return ContinuousRumbleEffect.builder()
                .constant(state)
                .inWorld(() -> originVec3, 0, 1, effectRange, Easings::easeInSine)
                .timeout(duration)
                .build();
    }
}
