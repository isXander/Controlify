package dev.isxander.splitscreen.client;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

/**
 * Represents the position and size of each window that makes
 * up the splitscreen.
 */
public sealed interface SplitscreenPosition {
    Visible FULL = new Visible(0, 0, 1, 1);
    Hidden HIDDEN = Hidden.INSTANCE;

    /* 2-player left-right splitscreen */
    Visible LEFT = new Visible(0, 0, 2, 1);
    Visible RIGHT = new Visible(1, 0, 2, 1);
    Visible[] LEFT_RIGHT = new Visible[]{LEFT, RIGHT};

    /* 2-player top-bottom splitscreen */
    Visible TOP = new Visible(0, 0, 1, 2);
    Visible BOTTOM = new Visible(0, 1, 1, 2);
    Visible[] TOP_BOTTOM = new Visible[]{TOP, BOTTOM};

    /* 4-player splitscreen */
    Visible TOP_LEFT = new Visible(0, 0, 2, 2);
    Visible TOP_RIGHT = new Visible(1, 0, 2, 2);
    Visible BOTTOM_LEFT = new Visible(0, 1, 2, 2);
    Visible BOTTOM_RIGHT = new Visible(1, 1, 2, 2);
    Visible[] FOUR_WAY = new Visible[]{TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT};

    /* 3-player side-by-side splitscreen */
    Visible LEFT_THIRD = new Visible(0, 0, 3, 1);
    Visible CENTER_THIRD = new Visible(1, 0, 3, 1);
    Visible RIGHT_THIRD = new Visible(2, 0, 3, 1);
    Visible[] LEFT_CENTER_RIGHT = new Visible[]{LEFT_THIRD, CENTER_THIRD, RIGHT_THIRD};

    /* 3-player unbalanced splitscreen */
    Visible[] LEFT_TOP_BOTTOM = new Visible[]{LEFT, TOP_RIGHT, BOTTOM_RIGHT};
    /* 3-balance unbalanced splitscreen */
    Visible[] LEFT_RIGHT_BOTTOM = new Visible[]{TOP_LEFT, TOP_RIGHT, BOTTOM};

    StreamCodec<ByteBuf, SplitscreenPosition> STREAM_CODEC =
            ByteBufCodecs.either(Visible.STREAM_CODEC, Hidden.STREAM_CODEC)
                    .map(
                            either -> either.map(Function.identity(), Function.identity()),
                            pos -> switch (pos) {
                                case Visible visible -> Either.left(visible);
                                case Hidden ignored -> Either.right(HIDDEN);
                            }
                    );

    /**
     * Represents a visible splitscreen position.
     * @param x the cell x coordinate
     * @param y the cell y coordinate
     * @param width the amount of cells to occupy in the x direction
     * @param height the amount of cells to occupy in the y direction
     * @param cellCountX the total cell count making up the window width
     * @param cellCountY the total cell count making up the window height
     */
    record Visible(int x, int y, int width, int height, int cellCountX, int cellCountY) implements SplitscreenPosition {
        public static final StreamCodec<ByteBuf, Visible> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT,
                        Visible::x,
                        ByteBufCodecs.INT,
                        Visible::y,
                        ByteBufCodecs.INT,
                        Visible::width,
                        ByteBufCodecs.INT,
                        Visible::height,
                        ByteBufCodecs.INT,
                        Visible::cellCountX,
                        ByteBufCodecs.INT,
                        Visible::cellCountY,
                        Visible::new
                );

        public Visible(int x, int y, int cellCountX, int cellCountY) {
            this(x, y, 1, 1, cellCountX, cellCountY);
        }

        public ScreenRectangle applyToRealDims(int x, int y, int width, int height) {
            int quadrantWidth = width / cellCountX;
            int quadrantHeight = height / cellCountY;
            int realX = x + this.x * quadrantWidth;
            int realY = y + this.y * quadrantHeight;
            return new ScreenRectangle(realX, realY, quadrantWidth, quadrantHeight);
        }

        public ScreenRectangle applyToRealDims(int x, int y, int width, int height, int paddingBetween, int paddingEdge) {
            int totalPaddingX = paddingEdge * 2 + paddingBetween * (cellCountX - 1);
            int totalPaddingY = paddingEdge * 2 + paddingBetween * (cellCountY - 1);
            int quadrantWidth = (width - totalPaddingX) / cellCountX;
            int quadrantHeight = (height - totalPaddingY) / cellCountY;
            int realX = x + paddingEdge + this.x * (quadrantWidth + paddingBetween);
            int realY = y + paddingEdge + this.y * (quadrantHeight + paddingBetween);
            return new ScreenRectangle(realX, realY, quadrantWidth, quadrantHeight);
        }

        public static Visible[] arrangeForN(int n, boolean preferHorizontal) {
            return switch (n) {
                case 1 -> new SplitscreenPosition.Visible[]{SplitscreenPosition.FULL};
                case 2 -> preferHorizontal ? SplitscreenPosition.LEFT_RIGHT : SplitscreenPosition.TOP_BOTTOM;
                case 3 -> preferHorizontal ? SplitscreenPosition.LEFT_TOP_BOTTOM : SplitscreenPosition.LEFT_RIGHT_BOTTOM;
                case 4 -> SplitscreenPosition.FOUR_WAY;
                default -> SplitscreenPosition.Visible.arrangeInGridForN(n);
            };
        }

        public static Visible[] arrangeInGridForN(int n) {
            int cellCountX = (int) Math.ceil(Math.sqrt(n));
            int cellCountY = (int) Math.ceil((double) n / cellCountX);

            Visible[] positions = new Visible[n];
            for (int i = 0; i < n; i++) {
                int x = i % cellCountX;
                int y = i / cellCountX;
                positions[i] = new Visible(x, y, cellCountX, cellCountY);
            }

            return positions;
        }
    }

    record Hidden() implements SplitscreenPosition {
        private static final Hidden INSTANCE = new Hidden();

        public static final StreamCodec<ByteBuf, Hidden> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    }
}
