package dev.isxander.controlify.api.contextual;

import net.minecraft.resources.Identifier;

public interface ContextualDomainRegistry {
	ContextualDomain<InGameContext> inGame();

	ContextualDomain<ContainerContext> container();

	<C extends Context> ContextualDomain<C> register(Identifier domainId);

	default <C extends Context> ContextualDomain<C> register(Identifier domainId, ContextualStateContributor<? super C> contributor) {
		var domain = this.<C>register(domainId);
		domain.registerContributor(contributor);
		return domain;
	}
}


