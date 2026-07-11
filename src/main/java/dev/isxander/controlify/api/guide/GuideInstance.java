package dev.isxander.controlify.api.guide;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;

public interface GuideInstance<T extends FactCtx> {
    GuideDomain<T> domain();

    boolean update(T context, Font font);

    void extractRenderState(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast);
    Renderable renderable(boolean bottomAligned, boolean textContrast);
}
