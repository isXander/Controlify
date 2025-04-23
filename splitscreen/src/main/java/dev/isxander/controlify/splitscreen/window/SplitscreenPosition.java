package dev.isxander.controlify.splitscreen.window;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum SplitscreenPosition {
    FULL(0, 0, 1, 1),
    HIDDEN(0, 0, 0, 0),

    LEFT(0, 0, 0.5f, 1),
    RIGHT(0.5f, 0, 0.5f, 1),
    TOP(0f, 0f, 1f, 0.5f),
    BOTTOM(0f, 0.5f, 1, 0.5f),

    TOP_LEFT(0, 0, 0.5f, 0.5f),
    TOP_RIGHT(0.5f, 0, 0.5f, 0.5f),
    BOTTOM_LEFT(0f, 0.5f, 0.5f, 0.5f),
    BOTTOM_RIGHT(0.5f, 0.5f, 0.5f, 0.5f);

    private final float x, y;
    private final float width, height;

    SplitscreenPosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public ScreenRectangle applyToRealDims(int x, int y, int width, int height) {
        return new ScreenRectangle(x + (int) (this.x * width), y + (int) (this.y * height), (int) (this.width * width), (int) (this.height * height));
    }

    public static final Codec<SplitscreenPosition> CODEC = Codec.INT
            .comapFlatMap(
                    ordinal -> {
                        if (ordinal >= 0 && ordinal < SplitscreenPosition.values().length) {
                            return DataResult.success(SplitscreenPosition.values()[ordinal]);
                        } else {
                            return DataResult.error(() -> "Invalid ordinal");
                        }
                    },
                    Enum::ordinal
            );

    public static final StreamCodec<ByteBuf, SplitscreenPosition> STREAM_CODEC = ByteBufCodecs.INT
            .map(ordinal -> SplitscreenPosition.values()[ordinal], Enum::ordinal);
}
