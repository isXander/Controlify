package dev.isxander.controlify.server.packets;

import dev.isxander.controlify.haptics.rumble.DynamicRumbleEffect;
import dev.isxander.controlify.haptics.rumble.RumbleEffect;
import dev.isxander.controlify.haptics.HapticSource;
import dev.isxander.controlify.haptics.rumble.RumbleState;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public record EntityVibrationPacket(int entityId, float range, int duration, RumbleState state, HapticSource source) {
    public static final Identifier CHANNEL = CUtil.rl("vibrate_from_entity");

    public static final StreamCodec<FriendlyByteBuf, EntityVibrationPacket> CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeInt(packet.entityId());
            buf.writeFloat(packet.range());
            buf.writeInt(packet.duration());
            buf.writeInt(RumbleState.packToInt(packet.state()));
            buf.writeIdentifier(packet.source().id());
        },
        buf -> new EntityVibrationPacket(
            buf.readInt(),
            buf.readFloat(),
            buf.readInt(),
            RumbleState.unpackFromInt(buf.readInt()),
            HapticSource.get(buf.readIdentifier())
        )
    );

    public RumbleEffect createEffect() {
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        return DynamicRumbleEffect.builder()
                .constant(state)
                .inWorld(entity::position, 0, 1, range, Easings.toFloat(Easings::easeInSine))
                .timeout(duration)
                .build();
    }
}
