package dev.isxander.controlify.gui.layout;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2ic;

public interface RenderComponent {
    void render(GuiGraphics graphics, int x, int y, float deltaTime);

    Vector2ic size();

    default boolean isVisible() {
        return true;
    }
}
