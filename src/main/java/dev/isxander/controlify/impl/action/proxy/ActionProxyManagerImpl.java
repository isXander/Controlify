/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.proxy;

import dev.isxander.controlify.api.action.ActionProxyManager;
import dev.isxander.controlify.impl.action.ActionAccessorImpl;
import dev.isxander.controlify.impl.action.ActionSpecRegistryImpl;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.utils.event.EventBus;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ActionProxyManagerImpl implements ActionProxyManager {
	/// Subscriptions do not initialize the proxy manager singleton.
	public static final class Events {
		public static final EventBus<ActionProxyManager> REGISTER_PROXIES = new EventBus<>();

		private Events() {
		}
	}

	public static final ActionProxyManagerImpl INSTANCE = new ActionProxyManagerImpl();

	private final Set<KeyMapping> excludedKeyMappings = new HashSet<>();
	private final Map<KeyMapping, Identifier> proxiedKeyMappings = new HashMap<>();
	private final Map<Identifier, KeyMappingProxy> keyMappingProxies = new LinkedHashMap<>();
	private final EventBus<ControllerImpl> proxyUpdates = new EventBus<>();
	private boolean locked;

	private ActionProxyManagerImpl() {
	}

	@Override
	public void dontAutoProxyKeyMapping(@NonNull KeyMapping keyMapping) {
		this.checkRegistrationOpen();
		this.excludedKeyMappings.add(Objects.requireNonNull(keyMapping));
	}

	@Override
	public void proxyKeyMapping(@NonNull Identifier actionId, @NonNull KeyMapping keyMapping) {
		this.checkRegistrationOpen();
		Objects.requireNonNull(actionId);
		Objects.requireNonNull(keyMapping);
		if (!ActionSpecRegistryImpl.INSTANCE.isRegistered(actionId)) {
			throw new IllegalArgumentException("Action is not registered: " + actionId);
		}
		Identifier previousAction = this.proxiedKeyMappings.get(keyMapping);
		if (previousAction != null && !previousAction.equals(actionId)) {
			throw new IllegalArgumentException("Key mapping is already proxied by " + previousAction);
		}
		KeyMappingProxy proxy = this.keyMappingProxies.computeIfAbsent(actionId, id -> {
			KeyMappingProxy listener = new KeyMappingProxy(new ActionAccessorImpl(id));
			this.proxyUpdates.subscribe(listener);
			return listener;
		});
		proxy.addKeyMapping(keyMapping);
		this.proxiedKeyMappings.put(keyMapping, actionId);
	}

	public boolean shouldAutomaticallyProxyKeyMapping(KeyMapping keyMapping) {
		return !this.excludedKeyMappings.contains(keyMapping) && !this.proxiedKeyMappings.containsKey(keyMapping);
	}

	public boolean isKeyMappingProxy(Identifier actionId) {
		return this.keyMappingProxies.containsKey(actionId);
	}

	public void update(ControllerImpl controller) {
		this.proxyUpdates.emit(controller);
	}

	public void release(ControllerImpl controller) {
		for (KeyMappingProxy proxy : this.keyMappingProxies.values()) {
			proxy.release(controller);
		}
	}

	public void lock() {
		this.locked = true;
	}

	private void checkRegistrationOpen() {
		if (this.locked) {
			throw new IllegalStateException("Action proxy registration has closed");
		}
	}
}
