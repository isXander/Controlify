package dev.isxander.controlify.utils.render;

public sealed interface SpriteScaling {
    record Stretch() implements SpriteScaling {}

    record Tiled(int width, int height) implements SpriteScaling {}

    record NineSlice(int width, int height, Border border) implements SpriteScaling {
        public NineSlice(int width, int height, int border) {
            this(width, height, new Border(border, border, border, border));
        }

        public record Border(int left, int right, int top, int bottom) {}
    }
}
