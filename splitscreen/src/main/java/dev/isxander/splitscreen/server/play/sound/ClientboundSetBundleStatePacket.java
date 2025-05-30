package dev.isxander.splitscreen.server.play.sound;

import dev.isxander.splitscreen.util.CSUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * Send to the client when the server is going to send certain
 * types of packets in their "bundled" form, hinting to clients to ignore
 * their regular counterparts.
 *
 * @param sound if sound packets will be bundled
 */
public record ClientboundSetBundleStatePacket(boolean sound) implements CustomPacketPayload {

    public static final StreamCodec<FriendlyByteBuf, ClientboundSetBundleStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientboundSetBundleStatePacket::sound,
            ClientboundSetBundleStatePacket::new
    );

    public static final Type<ClientboundSetBundleStatePacket> TYPE = new Type<>(CSUtil.rl("set_bundle_state"));

    @Override
    public @NotNull Type<ClientboundSetBundleStatePacket> type() {
        return TYPE;
    }
}
