package dev.isxander.controlify.splitscreen.engine.impl.fboshare.b3dext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SharedRenderTarget(
        SharedTexture colour,
        SharedTexture depth,
        int width, int height
) {
    public static final StreamCodec<FriendlyByteBuf, SharedRenderTarget> STREAM_CODEC = StreamCodec.composite(
            SharedTexture.STREAM_CODEC,
            SharedRenderTarget::colour,
            SharedTexture.STREAM_CODEC,
            SharedRenderTarget::depth,
            ByteBufCodecs.INT,
            SharedRenderTarget::width,
            ByteBufCodecs.INT,
            SharedRenderTarget::height,
            SharedRenderTarget::new
    );

    public int getAllocationSize() {
        int ppf = width() * height();
        return colour().format().pixelSize() * ppf +
                depth().format().pixelSize() * ppf;
    }
}
