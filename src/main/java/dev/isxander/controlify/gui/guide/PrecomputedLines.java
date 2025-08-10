package dev.isxander.controlify.gui.guide;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record PrecomputedLines(List<PrecomputedLine> lines, int width, int height) {
    public static final PrecomputedLines EMPTY = new PrecomputedLines(List.of(), 0, 0);

    public record PrecomputedLine(Component text, int width, int height, int backgroundLeft, int backgroundRight) {
    }

    public static class Builder {
        private final List<PrecomputedLine> lines = new ArrayList<>(5);
        private int width = 0;
        private int height = 0;

        public Builder addLine(Component text, int width, int height, int backgroundLeft, int backgroundRight) {
            lines.add(new PrecomputedLine(text, width, height, backgroundLeft, backgroundRight));
            this.width = Math.max(this.width, width);
            this.height += height;
            return this;
        }

        public PrecomputedLines build() {
            return new PrecomputedLines(Collections.unmodifiableList(lines), width, height);
        }
    }
}
