package dev.isxander.controlify.server.packets;

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

        buf.writeInt(frames.length); // amount of frames
        for (RumbleState frame : frames) {
            buf.writeInt(RumbleState.packToInt(frame));
        }
    }

    public RumbleEffect createEffect() {
        return new BasicRumbleEffect(frames).earlyFinish(() -> Minecraft.getInstance().level == null);
    }

    private static RumbleState[] readFrames(FriendlyByteBuf buf) {
        RumbleState[] frames = new RumbleState[buf.readInt()]; // read the length

        for (int i = 0; i < frames.length; i++) {
            frames[i] = RumbleState.unpackFromInt(buf.readInt());
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
