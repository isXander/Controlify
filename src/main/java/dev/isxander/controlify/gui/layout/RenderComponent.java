package dev.isxander.controlify.gui.layout;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector2ic;

public interface RenderComponent {
    void render(PoseStack stack, int x, int y, float deltaTime);

    Vector2ic size();

    default boolean isVisible() {
        return true;
    }
}
