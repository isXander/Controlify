package dev.isxander.controlify.contextual.api;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;

public interface ContextualDomain<C extends Context> {
	Identifier id();

	void registerContributor(ContextualStateContributor<? super C> contributor);

	GuideInstance<C> createGuideInstance(Font font);
}
