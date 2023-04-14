package dev.isxander.controlify.api.bind;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.gui.DrawSize;

public interface BindRenderer {
    DrawSize size();

    void render(PoseStack poseStack, int x, int centerY);
}
