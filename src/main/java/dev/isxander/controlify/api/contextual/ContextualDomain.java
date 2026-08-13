package dev.isxander.controlify.api.contextual;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public interface ContextualDomain<C extends Context> {
	Identifier id();

	void registerContributor(ContextualStateContributor<? super C> contributor);

	GuideInstance<C> createGuideInstance(Font font);

	@ApiStatus.Experimental
	TriggerEffectInstance<C> createTriggerEffectInstance();
}
