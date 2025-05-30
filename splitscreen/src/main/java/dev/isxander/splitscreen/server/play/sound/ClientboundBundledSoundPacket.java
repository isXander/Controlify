package dev.isxander.splitscreen.server.play.sound;

import dev.isxander.splitscreen.server.BundledPacketInfo;
import dev.isxander.splitscreen.util.CSUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.jetbrains.annotations.NotNull;

public record ClientboundBundledSoundPacket(BundledPacketInfo bundleInfo, ClientboundSoundPacket packet) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBundledSoundPacket> STREAM_CODEC = StreamCodec.composite(
            BundledPacketInfo.STREAM_CODEC,
            ClientboundBundledSoundPacket::bundleInfo,
            ClientboundSoundPacket.STREAM_CODEC,
            ClientboundBundledSoundPacket::packet,
            ClientboundBundledSoundPacket::new
    );

    public static final Type<ClientboundBundledSoundPacket> TYPE = new Type<>(CSUtil.rl("bundled_sound"));

    @Override
    public @NotNull Type<ClientboundBundledSoundPacket> type() {
        return TYPE;
    }
}
