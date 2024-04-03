package dev.isxander.controlify.server;

import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/*? if >1.20.4 {*/
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/*? } else {*//*
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
*//*? }*/

public record EntityVibrationPacket(int entityId, float range, int duration, RumbleState state, RumbleSource source)
        /*? if >1.20.4 {*/
        implements CustomPacketPayload
        /*? } else {*//*
        implements FabricPacket
        *//*? }*/
{
    private static final ResourceLocation ID = new ResourceLocation("controlify", "vibrate_from_entity");

    /*? if >1.20.4 {*/
    public static final CustomPacketPayload.Type<EntityVibrationPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityVibrationPacket> CODEC = StreamCodec.ofMember(EntityVibrationPacket::write, EntityVibrationPacket::new);
    /*? } else {*//*
    public static final PacketType<EntityVibrationPacket> TYPE = PacketType.create(ID, EntityVibrationPacket::new);
    *//*? }*/

    public EntityVibrationPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readFloat(), buf.readInt(), OriginVibrationPacket.readState(buf), RumbleSource.get(buf.readResourceLocation()));
    }

    /*? if <=1.20.4 {*//*
    @Override
    *//*? }*/
    public void write(FriendlyByteBuf buf)
    {
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

    /*? if >1.20.4 {*/
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    /*? } else {*//*
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
    *//*? }*/
}
