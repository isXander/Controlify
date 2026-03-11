package dev.isxander.controlify.gui.layout;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2ic;

public interface RenderComponent {
    void render(GuiGraphicsExtractor graphics, int x, int y, float deltaTime); // TODO render?

    Vector2ic size();

    default boolean isVisible() {
        return true;
    }
}
