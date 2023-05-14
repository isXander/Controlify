package dev.isxander.controlify.gui.layout;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
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

    public void updatePosition() {
        Vector2ic componentSize = component.size();

        Vector2ic windowPosition = windowAnchor.getAnchorPosition(Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
        Vector2ic anchoredPosition = origin.getAnchorPosition(componentSize.x(), componentSize.y());

        this.x = windowPosition.x() + offsetX - anchoredPosition.x();
        this.y = windowPosition.y() + offsetY - anchoredPosition.y();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderComponent(matrices, delta);
    }

    public void renderComponent(PoseStack stack, float deltaTime) {
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
}
