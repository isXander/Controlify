package dev.isxander.controlify.gui.layout;

import com.mojang.blaze3d.vertex.PoseStack;
import org.apache.commons.lang3.Validate;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public class ColumnLayoutComponent<T extends RenderComponent> extends AbstractLayoutComponent<T> {
    private final int componentPaddingVertical;
    private final int colPaddingLeft, colPaddingRight, colPaddingTop, colPaddingBottom;
    private final ElementPosition elementPosition;

    private ColumnLayoutComponent(Collection<? extends T> elements,
                                  int componentPaddingVertical,
                                  int colPaddingLeft, int colPaddingRight,
                                  int colPaddingTop, int colPaddingBottom,
                                  ElementPosition elementPosition
    ) {
        for (var element : elements) {
            insertTop(element);
        }

        this.componentPaddingVertical = componentPaddingVertical;
        this.colPaddingLeft = colPaddingLeft;
        this.colPaddingRight = colPaddingRight;
        this.colPaddingTop = colPaddingTop;
        this.colPaddingBottom = colPaddingBottom;
        this.elementPosition = elementPosition;
    }

    @Override
    public void render(PoseStack stack, int x, int y, float deltaTime) {
        int width = getMaxChildWidth();

        if (width == -1)
            return;

        int yOffset = 0;
        for (var element : getChildComponents()) {
            if (!element.isVisible())
                continue;

            element.render(
                    stack,
                    x + colPaddingLeft + elementPosition.positionFunction.apply(width, element.size().x()),
                    y + colPaddingTop + yOffset,
                    deltaTime
            );

            yOffset += element.size().y() + componentPaddingVertical;
        }
    }

    @Override
    public Vector2ic size() {
        return new Vector2i(
                getMaxChildWidth() + colPaddingLeft + colPaddingRight,
                getSumHeight() + colPaddingTop + colPaddingBottom
        );
    }

    private int getSumHeight() {
        return this.getChildComponents().stream()
                .filter(RenderComponent::isVisible)
                .map(RenderComponent::size)
                .mapToInt(size -> size.y() + componentPaddingVertical)
                .sum() - componentPaddingVertical;
    }

    private int getMaxChildWidth() {
        return this.getChildComponents().stream()
                .filter(RenderComponent::isVisible)
                .map(RenderComponent::size)
                .mapToInt(Vector2ic::x)
                .max().orElse(-1);
    }

    public static <T extends RenderComponent> Builder<T> builder() {
        return new Builder<>();
    }

    public enum ElementPosition {
        LEFT((rowWidth, elementWidth) -> 0),
        RIGHT((rowWidth, elementWidth) -> rowWidth - elementWidth),
        MIDDLE((rowWidth, elementWidth) -> rowWidth / 2 - elementWidth / 2);

        public final BiFunction<Integer, Integer, Integer> positionFunction;

        ElementPosition(BiFunction<Integer, Integer, Integer> positionFunction) {
            this.positionFunction = positionFunction;
        }
    }

    public static class Builder<T extends RenderComponent> {
        private final List<T> elements = new ArrayList<>();
        private int componentPaddingVertical;
        private int colPaddingLeft, colPaddingRight, colPaddingTop, colPaddingBottom;
        private ElementPosition elementPosition = null;

        public Builder<T> element(T element) {
            elements.add(element);
            return this;
        }

        public Builder<T> elements(T... elements) {
            this.elements.addAll(Arrays.asList(elements));
            return this;
        }

        public Builder<T> elements(Collection<? extends T> elements) {
            this.elements.addAll(elements);
            return this;
        }

        public Builder<T> elementPadding(int padding) {
            this.componentPaddingVertical = padding;
            return this;
        }

        public Builder<T> colPadding(int left, int right, int top, int bottom) {
            this.colPaddingLeft = left;
            this.colPaddingRight = right;
            this.colPaddingTop = top;
            this.colPaddingBottom = bottom;
            return this;
        }

        public Builder<T> colPadding(int horizontal, int vertical) {
            return colPadding(horizontal, horizontal, vertical, vertical);
        }

        public Builder<T> colPadding(int padding) {
            return colPadding(padding, padding, padding, padding);
        }

        public Builder<T> elementPosition(ElementPosition elementPosition) {
            this.elementPosition = elementPosition;
            return this;
        }

        public ColumnLayoutComponent<T> build() {
            Validate.notEmpty(elements, "No elements were added to the column!");
            Validate.notNull(elementPosition, "Element position cannot be null!");

            return new ColumnLayoutComponent<>(
                    elements,
                    componentPaddingVertical,
                    colPaddingLeft, colPaddingRight,
                    colPaddingTop, colPaddingBottom,
                    elementPosition
            );
        }
    }
}
