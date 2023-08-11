package dev.isxander.controlify.server;

import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.Easings;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record EntityVibrationPacket(int entityId, float range, int duration, RumbleState state, RumbleSource source) implements FabricPacket {
    public static final PacketType<EntityVibrationPacket> TYPE = PacketType.create(new ResourceLocation("controlify", "vibrate_from_entity"), EntityVibrationPacket::new);

    public EntityVibrationPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readFloat(), buf.readInt(), OriginVibrationPacket.readState(buf), RumbleSource.get(buf.readResourceLocation()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeFloat(range);
        buf.writeInt(duration);

        int high = (int)(state.strong() * 32767.0F);
        int low = (int)(state.weak() * 32767.0F);
        buf.writeInt((high << 16) | (low & 0xFFFF));

        buf.writeResourceLocation(source.id());
    }

    public RumbleEffect createEffect() {
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        return ContinuousRumbleEffect.builder()
                .constant(state)
                .inWorld(entity::position, 0, 1, range, Easings::easeInSine)
                .timeout(duration)
                .build();
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
