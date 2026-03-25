package dev.isxander.controlify.gui.layout;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.joml.Vector2ic;
import org.jspecify.annotations.NonNull;

public class PositionedComponent<T extends RenderComponent> implements Renderable, GuiEventListener, NarratableEntry {
    private final T component;

    private int x, y;

    private final AnchorPoint windowAnchor;
    private final int offsetX, offsetY;
    private final AnchorPoint origin;

    public PositionedComponent(T component, AnchorPoint windowAnchor, int offsetX, int offsetY, AnchorPoint origin) {
        this.component = component;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.windowAnchor = windowAnchor;
        this.origin = origin;
    }

    public void updatePosition(int windowWidth, int windowHeight) {
        Vector2ic componentSize = component.size();

        Vector2ic windowPosition = windowAnchor.getAnchorPosition(windowWidth, windowHeight);
        Vector2ic anchoredPosition = origin.getAnchorPosition(componentSize.x(), componentSize.y());

        this.x = windowPosition.x() + offsetX - anchoredPosition.x();
        this.y = windowPosition.y() + offsetY - anchoredPosition.y();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractComponentRenderState(graphics, a);
    }

    public void extractComponentRenderState(GuiGraphicsExtractor graphics, float deltaTime) {
        component.extractRenderState(graphics, x, y, deltaTime);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public T getComponent() {
        return component;
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public @NonNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NonNull NarrationElementOutput builder) {

    }
}
