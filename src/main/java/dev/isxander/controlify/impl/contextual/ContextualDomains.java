/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.contextual;

import dev.isxander.controlify.api.contextual.*;
import dev.isxander.controlify.impl.platform.client.PlatformClientUtil;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.event.EventBus;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class ContextualDomains implements ContextualDomainRegistry {
	/// Loading this holder does not construct domains or register resource listeners.
	public static final class Events {
		public static final EventBus<ContextualDomainRegistry> REGISTER_DOMAINS = new EventBus<>();

		private Events() {}
	}

	public static final ContextualDomains INSTANCE = new ContextualDomains();

	private final List<ContextualDomainImpl<?>> domains = new ArrayList<>();
	private boolean locked;

	private final ContextualDomainImpl<InGameContext> inGame = register(
			CUtil.rl("in_game"),
			ContextualStateContributors.IN_GAME
	);
	private final ContextualDomainImpl<ContainerContext> container = register(
			CUtil.rl("container"),
			ContextualStateContributors.CONTAINER
	);

	public @NonNull ContextualDomainImpl<InGameContext> inGame() {
		return this.inGame;
	}

	public @NonNull ContextualDomainImpl<ContainerContext> container() {
		return this.container;
	}

	@Override
	public <C extends Context> @NonNull ContextualDomainImpl<C> register(@NonNull Identifier domainId) {
		if (this.locked) {
			throw new IllegalStateException("Contextual domains must be registered during REGISTER_DOMAINS");
		}
		if (this.domains.stream().anyMatch(domain -> domain.id().equals(domainId))) {
			throw new IllegalArgumentException("Contextual domain is already registered: " + domainId);
		}
		var domain = new ContextualDomainImpl<C>(domainId);
		this.domains.add(domain);
		PlatformClientUtil.registerAssetReloadListener(domain);
		return domain;
	}

	@Override
	public <C extends Context> @NonNull ContextualDomainImpl<C> register(@NonNull Identifier domainId, @NonNull ContextualStateContributor<? super C> contributor) {
		var domain = this.<C>register(domainId);
		domain.registerContributor(contributor);
		return domain;
	}

	public void invalidateResolvedFactGraphs() {
		this.domains.forEach(ContextualDomainImpl::invalidateResolvedFactGraph);
	}

	public static void touch() {}

	public void lock() {
		this.locked = true;
	}

	private ContextualDomains() {
	}
}
