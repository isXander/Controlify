package dev.isxander.controlify.server.packets;

import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.server.CSUtil;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public record EntityVibrationPacket(int entityId, float range, int duration, RumbleState state, RumbleSource source) {
    public static final Identifier CHANNEL = CUtil.rl("vibrate_from_entity");

    public static final StreamCodec<FriendlyByteBuf, EntityVibrationPacket> CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeInt(packet.entityId());
            buf.writeFloat(packet.range());
            buf.writeInt(packet.duration());
            buf.writeInt(RumbleState.packToInt(packet.state()));
            CSUtil.writeIdentifier(buf, packet.source().id());
        },
        buf -> new EntityVibrationPacket(
            buf.readInt(),
            buf.readFloat(),
            buf.readInt(),
            RumbleState.unpackFromInt(buf.readInt()),
            RumbleSource.get(CSUtil.readIdentifier(buf))
        )
    );

    public RumbleEffect createEffect() {
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        return ContinuousRumbleEffect.builder()
                .constant(state)
                .inWorld(entity::position, 0, 1, range, Easings.toFloat(Easings::easeInSine))
                .timeout(duration)
                .build();
    }
}
