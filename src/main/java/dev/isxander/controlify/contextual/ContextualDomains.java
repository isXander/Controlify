package dev.isxander.controlify.contextual;

import dev.isxander.controlify.api.contextual.ContainerContext;
import dev.isxander.controlify.api.contextual.InGameContext;
import dev.isxander.controlify.api.contextual.Context;
import dev.isxander.controlify.api.contextual.ContextualDomain;
import dev.isxander.controlify.api.contextual.ContextualDomainRegistry;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public final class ContextualDomains implements ContextualDomainRegistry {

	public static final ContextualDomains INSTANCE = new ContextualDomains();

	private final ContextualDomain<InGameContext> inGame = register(
			CUtil.rl("in_game"),
			ContextualStateContributors.IN_GAME
	);
	private final ContextualDomain<ContainerContext> container = register(
			CUtil.rl("container"),
			ContextualStateContributors.CONTAINER
	);

	@Override
	public ContextualDomain<InGameContext> inGame() {
		return this.inGame;
	}

	@Override
	public ContextualDomain<ContainerContext> container() {
		return this.container;
	}

	@Override
	public <C extends Context> ContextualDomain<C> register(Identifier domainId) {
		var domain = new ContextualDomainImpl<C>(domainId);
		PlatformClientUtil.registerAssetReloadListener(domain);
		return domain;
	}

	private ContextualDomains() {
	}
}
