/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame.look;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.ingame.look.LookRegistry;
import dev.isxander.controlify.api.ingame.look.LookScaler;
import dev.isxander.controlify.api.ingame.look.LookScalerFactory;
import dev.isxander.controlify.api.ingame.look.LookSource;
import dev.isxander.controlify.api.ingame.look.LookSourceFactory;
import dev.isxander.controlify.impl.utils.event.EventBus;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// Holds the factories of look sources and scalers. Built-ins register through the same
/// path as addons.
public final class LookRegistryImpl implements LookRegistry {
	/// Loading this holder does not construct the registry.
	public static final class Events {
		public static final EventBus<LookRegistry> REGISTER = new EventBus<>();

		private Events() {
		}
	}

	public static final LookRegistryImpl INSTANCE = new LookRegistryImpl();

	private final Map<Identifier, LookSourceFactory> sources = new LinkedHashMap<>();
	private final Map<Identifier, LookScalerFactory> scalers = new LinkedHashMap<>();
	private boolean locked;

	public LookRegistryImpl() {
	}

	/// Registers Controlify's own sources, then lets addons register theirs.
	/// Must be called exactly once during early setup.
	public void initialise() {
		BuiltinLook.register(this);
		try {
			Events.REGISTER.emit(this);
		} finally {
			this.locked = true;
		}
	}

	@Override
	public synchronized void registerSource(@NonNull Identifier id, @NonNull LookSourceFactory factory) {
		this.requireUnlocked();
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(factory, "factory");
		if (this.sources.putIfAbsent(id, factory) != null) {
			throw new IllegalStateException("Look source already registered: " + id);
		}
	}

	@Override
	public synchronized void registerScaler(@NonNull Identifier id, @NonNull LookScalerFactory factory) {
		this.requireUnlocked();
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(factory, "factory");
		if (this.scalers.putIfAbsent(id, factory) != null) {
			throw new IllegalStateException("Look scaler already registered: " + id);
		}
	}

	/// @return a new instance of every registered source for this controller
	public synchronized List<LookSource> createSources(Controller controller) {
		List<LookSource> result = new ArrayList<>(this.sources.size());
		for (LookSourceFactory factory : this.sources.values()) {
			result.add(factory.create(controller));
		}
		return result;
	}

	/// @return a new instance of every registered scaler for this controller
	public synchronized List<LookScaler> createScalers(Controller controller) {
		List<LookScaler> result = new ArrayList<>(this.scalers.size());
		for (LookScalerFactory factory : this.scalers.values()) {
			result.add(factory.create(controller));
		}
		return result;
	}

	private void requireUnlocked() {
		if (this.locked) {
			throw new IllegalStateException("Look sources and scalers must be registered during LookEvents.REGISTER");
		}
	}
}
