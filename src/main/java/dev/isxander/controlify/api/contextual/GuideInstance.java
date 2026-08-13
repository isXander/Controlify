package dev.isxander.controlify.api.contextual;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;

public interface GuideInstance<C extends Context> extends ContextualInstance<C> {
	void extractRenderState(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast, int guiScale);

	default void extractRenderState(GuiGraphicsExtractor graphics, boolean bottomAligned, boolean textContrast) {
		extractRenderState(graphics, bottomAligned, textContrast, -1);
	}

	Renderable renderable(boolean bottomAligned, boolean textContrast, int guiScale);

	default Renderable renderable(boolean bottomAligned, boolean textContrast) {
		return renderable(bottomAligned, textContrast, -1);
	}
}
