package dev.isxander.controlify.splitscreen.server.play.sound;

import dev.isxander.controlify.splitscreen.server.BundledPacketInfo;
import dev.isxander.controlify.splitscreen.util.CSUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import org.jetbrains.annotations.NotNull;

public record ClientboundBundledSoundEntityPacket(BundledPacketInfo bundleInfo, ClientboundSoundEntityPacket packet) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBundledSoundEntityPacket> STREAM_CODEC = StreamCodec.composite(
            BundledPacketInfo.STREAM_CODEC,
            ClientboundBundledSoundEntityPacket::bundleInfo,
            ClientboundSoundEntityPacket.STREAM_CODEC,
            ClientboundBundledSoundEntityPacket::packet,
            ClientboundBundledSoundEntityPacket::new
    );

    public static final Type<ClientboundBundledSoundEntityPacket> TYPE = new Type<>(CSUtil.rl("bundled_sound_entity"));

    @Override
    public @NotNull Type<ClientboundBundledSoundEntityPacket> type() {
        return TYPE;
    }
}
