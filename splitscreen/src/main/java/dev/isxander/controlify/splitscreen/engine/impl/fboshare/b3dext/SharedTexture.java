package dev.isxander.controlify.splitscreen.engine.impl.fboshare.b3dext;

import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SharedTexture(
        ShareHandle handle,
        TextureFormat format
) {
    public static final StreamCodec<FriendlyByteBuf, SharedTexture> STREAM_CODEC = StreamCodec.composite(
            ShareHandle.CODEC,
            SharedTexture::handle,
            ByteBufCodecs.INT.map(ordinal -> TextureFormat.values()[ordinal], Enum::ordinal),
            SharedTexture::format,
            SharedTexture::new
    );
}
