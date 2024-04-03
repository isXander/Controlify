package dev.isxander.controlify.server;

import dev.isxander.controlify.rumble.*;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/*? if >1.20.4 {*/
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/*? } else {*//*
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
*//*? }*/

public record OriginVibrationPacket(Vector3f origin, float effectRange, int duration, RumbleState state, RumbleSource source)
        /*? if >1.20.4 {*/
        implements CustomPacketPayload
        /*? } else {*//*
        implements FabricPacket
        *//*? }*/
{
    private static final ResourceLocation ID = new ResourceLocation("controlify", "vibrate_from_origin");

    /*? if >1.20.4 {*/
    public static final CustomPacketPayload.Type<OriginVibrationPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, OriginVibrationPacket> CODEC = StreamCodec.ofMember(OriginVibrationPacket::write, OriginVibrationPacket::new);
    /*? } else {*//*
    public static final PacketType<OriginVibrationPacket> TYPE = PacketType.create(ID, OriginVibrationPacket::new);
    *//*? }*/

    public OriginVibrationPacket(FriendlyByteBuf buf) {
        this(buf.readVector3f(), buf.readFloat(), buf.readVarInt(), readState(buf), RumbleSource.get(buf.readResourceLocation()));
    }

    /*? if <=1.20.4 {*//*
    @Override
    *//*? }*/
    public void write(FriendlyByteBuf buf) {
        buf.writeVector3f(origin);
        buf.writeFloat(effectRange);
        buf.writeVarInt(duration);

        int high = (int)(state.strong() * 32767.0F);
        int low = (int)(state.weak() * 32767.0F);
        buf.writeInt((high << 16) | (low & 0xFFFF));

        buf.writeResourceLocation(source.id());
    }

    public RumbleEffect createEffect() {
        var originVec3 = new Vec3(origin);
        return ContinuousRumbleEffect.builder()
                .constant(state)
                .inWorld(() -> originVec3, 0, 1, effectRange, Easings::easeInSine)
                .timeout(duration)
                .build();
    }

    public static RumbleState readState(FriendlyByteBuf buf) {
        int packed = buf.readInt();
        float strong = (short)(packed >> 16) / 32767.0F;
        float weak = (short)packed / 32767.0F;
        return new RumbleState(strong, weak);
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
