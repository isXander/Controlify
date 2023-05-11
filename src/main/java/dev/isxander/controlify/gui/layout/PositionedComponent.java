package dev.isxander.controlify.gui.layout;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class PositionedComponent<T extends RenderComponent> {
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

        updatePosition();
    }

    public void updatePosition() {
        Vector2ic windowPosition = windowAnchor.getAnchorPosition(windowSize());
        Vector2ic anchoredPosition = origin.getAnchorPosition(component.size());

        this.x = windowPosition.x() + offsetX - anchoredPosition.x();
        this.y = windowPosition.y() + offsetY - anchoredPosition.y();
    }

    public void render(PoseStack stack, float deltaTime) {
        component.render(stack, x, y, deltaTime);
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

    private Vector2i windowSize() {
        Window window = Minecraft.getInstance().getWindow();
        return new Vector2i(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }
}
