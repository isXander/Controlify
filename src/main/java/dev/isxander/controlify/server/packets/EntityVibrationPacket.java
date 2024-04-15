package dev.isxander.controlify.server.packets;

import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record EntityVibrationPacket(int entityId, float range, int duration, RumbleState state, RumbleSource source) {
    public static final ResourceLocation CHANNEL = new ResourceLocation("controlify", "vibrate_from_entity");

    public static final ControlifyPacketCodec<EntityVibrationPacket> CODEC = ControlifyPacketCodec.of(
        (buf, packet) -> {
            buf.writeInt(packet.entityId());
            buf.writeFloat(packet.range());
            buf.writeInt(packet.duration());
            buf.writeInt(RumbleState.packToInt(packet.state()));
            buf.writeResourceLocation(packet.source().id());
        },
        buf -> new EntityVibrationPacket(
            buf.readInt(),
            buf.readFloat(),
            buf.readInt(),
            RumbleState.unpackFromInt(buf.readInt()),
            RumbleSource.get(buf.readResourceLocation())
        )
    );

    public RumbleEffect createEffect() {
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        return ContinuousRumbleEffect.builder()
                .constant(state)
                .inWorld(entity::position, 0, 1, range, Easings::easeInSine)
                .timeout(duration)
                .build();
    }
}
