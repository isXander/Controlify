package dev.isxander.controlify.gui.layout;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import org.joml.Vector2ic;

public class PositionedComponent<T extends RenderComponent> implements Renderable {
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderComponent(graphics, delta);
    }

    public void renderComponent(GuiGraphics graphics, float deltaTime) {
        component.render(graphics, x, y, deltaTime);
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
}
