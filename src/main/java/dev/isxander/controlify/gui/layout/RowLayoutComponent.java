package dev.isxander.controlify.gui.layout;

import net.minecraft.client.gui.GuiGraphics;
import org.apache.commons.lang3.Validate;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;
import java.util.function.BiFunction;

public class RowLayoutComponent<T extends RenderComponent> extends AbstractLayoutComponent<T> {
    private final int elementPaddingHorizontal;
    private final int rowPaddingLeft, rowPaddingRight, rowPaddingTop, rowPaddingBottom;
    private final ElementPosition elementPosition;

    private RowLayoutComponent(Collection<? extends T> elements,
                               int elementPaddingHorizontal,
                               int rowPaddingLeft, int rowPaddingRight,
                               int rowPaddingTop, int rowPaddingBottom,
                               ElementPosition elementPosition
    ) {
        for (var element : elements) {
            insertTop(element);
        }

        this.elementPaddingHorizontal = elementPaddingHorizontal;
        this.rowPaddingLeft = rowPaddingLeft;
        this.rowPaddingRight = rowPaddingRight;
        this.rowPaddingTop = rowPaddingTop;
        this.rowPaddingBottom = rowPaddingBottom;
        this.elementPosition = elementPosition;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float deltaTime) {
        int height = getMaxChildHeight();

        if (height == -1)
            return;

        int xOffset = 0;
        for (var element : getChildComponents()) {
            if (!element.isVisible())
                continue;

            element.render(
                    graphics,
                    x + rowPaddingLeft + xOffset,
                    y + rowPaddingTop + elementPosition.positionFunction.apply(height, element.size().y()),
                    deltaTime
            );

            xOffset += element.size().x() + elementPaddingHorizontal;
        }
    }

    @Override
    public Vector2ic size() {
        return new Vector2i(
                getSumWidth() + rowPaddingLeft + rowPaddingRight,
                getMaxChildHeight() + rowPaddingTop + rowPaddingBottom
        );
    }

    private int getMaxChildHeight() {
        return this.getChildComponents().stream()
                .filter(RenderComponent::isVisible)
                .map(RenderComponent::size)
                .mapToInt(Vector2ic::y)
                .max().orElse(-1);
    }

    private int getSumWidth() {
        return this.getChildComponents().stream()
                .filter(RenderComponent::isVisible)
                .map(RenderComponent::size)
                .mapToInt(size -> size.x() + elementPaddingHorizontal)
                .sum() - elementPaddingHorizontal;
    }

    @Override
    public boolean isVisible() {
        return this.getChildComponents().stream()
                .anyMatch(RenderComponent::isVisible);
    }

    public static <T extends RenderComponent> Builder<T> builder() {
        return new Builder<>();
    }

    public enum ElementPosition {
        TOP((rowHeight, elementHeight) -> 0),
        BOTTOM((rowHeight, elementHeight) -> rowHeight - elementHeight),
        MIDDLE((rowHeight, elementHeight) -> rowHeight / 2 - elementHeight / 2);

        public final BiFunction<Integer, Integer, Integer> positionFunction;

        ElementPosition(BiFunction<Integer, Integer, Integer> positionFunction) {
            this.positionFunction = positionFunction;
        }
    }

    public static class Builder<T extends RenderComponent> {
        private final List<T> elements = new ArrayList<>();
        private int elementPaddingHorizontal;
        private int rowPaddingLeft, rowPaddingRight, rowPaddingTop, rowPaddingBottom;
        private ElementPosition elementPosition = null;

        public Builder<T> element(T element) {
            elements.add(element);
            return this;
        }

        @SafeVarargs
        public final Builder<T> elements(T... elements) {
            this.elements.addAll(Arrays.asList(elements));
            return this;
        }

        public Builder<T> elements(Collection<? extends T> elements) {
            this.elements.addAll(elements);
            return this;
        }

        public Builder<T> spacing(int padding) {
            this.elementPaddingHorizontal = padding;
            return this;
        }

        public Builder<T> rowPadding(int left, int right, int top, int bottom) {
            this.rowPaddingLeft = left;
            this.rowPaddingRight = right;
            this.rowPaddingTop = top;
            this.rowPaddingBottom = bottom;
            return this;
        }

        public Builder<T> rowPadding(int horizontal, int vertical) {
            return rowPadding(horizontal, horizontal, vertical, vertical);
        }

        public Builder<T> rowPadding(int padding) {
            return rowPadding(padding, padding, padding, padding);
        }

        public Builder<T> elementPosition(ElementPosition elementPosition) {
            this.elementPosition = elementPosition;
            return this;
        }

        public RowLayoutComponent<T> build() {
            Validate.notEmpty(elements, "No elements were added to the row!");
            Validate.notNull(elementPosition, "Element position cannot be null!");

            return new RowLayoutComponent<>(
                    elements,
                    elementPaddingHorizontal,
                    rowPaddingLeft, rowPaddingRight,
                    rowPaddingTop, rowPaddingBottom,
                    elementPosition
            );
        }
    }
}
