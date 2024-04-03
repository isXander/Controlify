package dev.isxander.controlify.server;

import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/*? if >1.20.4 {*/
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/*? } else {*//*
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
*//*? }*/

public record VibrationPacket(RumbleSource source, RumbleState[] frames)
        /*? if >1.20.4 {*/
        implements CustomPacketPayload
        /*? } else {*//*
        implements FabricPacket
        *//*? }*/
{
    public static final ResourceLocation ID = new ResourceLocation("controlify", "vibration");

    /*? if >1.20.4 {*/
    public static final CustomPacketPayload.Type<VibrationPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, VibrationPacket> CODEC = StreamCodec.ofMember(VibrationPacket::write, VibrationPacket::new);
    /*? } else {*//*
    public static final PacketType<VibrationPacket> TYPE = PacketType.create(ID, VibrationPacket::new);
    *//*? }*/
    public VibrationPacket(FriendlyByteBuf buf) {
        this(RumbleSource.get(buf.readResourceLocation()), readFrames(buf));
    }

    /*? if <=1.20.4 {*//*
    @Override
    *//*? }*/
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(source.id());

        int[] packedFrames = new int[frames.length];
        for (int i = 0; i < frames.length; i++) {
            RumbleState frame = frames[i];
            int high = (int)(frame.strong() * 32767.0F);
            int low = (int)(frame.weak() * 32767.0F);
            packedFrames[i] = (high << 16) | (low & 0xFFFF);
        }
        buf.writeVarIntArray(packedFrames);
    }

    public RumbleEffect createEffect() {
        return new BasicRumbleEffect(frames).earlyFinish(() -> Minecraft.getInstance().level == null);
    }

    private static RumbleState[] readFrames(FriendlyByteBuf buf) {
        int[] packedFrames = buf.readVarIntArray();
        RumbleState[] frames = new RumbleState[packedFrames.length];
        for (int i = 0; i < packedFrames.length; i++) {
            int packed = packedFrames[i];
            float strong = (short)(packed >> 16) / 32767.0F;
            float weak = (short)packed / 32767.0F;
            frames[i] = new RumbleState(strong, weak);
        }
        return frames;
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
