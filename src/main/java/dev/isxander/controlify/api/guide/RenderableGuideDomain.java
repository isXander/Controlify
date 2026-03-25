package dev.isxander.controlify.api.guide;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;

public interface RenderableGuideDomain<T extends FactCtx> extends GuideDomainRegistry<T> {
    void render(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast);

    Renderable renderable(boolean bottomAligned, boolean textContrast);
}
