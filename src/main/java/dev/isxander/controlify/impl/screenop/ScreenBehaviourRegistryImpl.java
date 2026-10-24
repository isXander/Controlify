/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.ScreenBehaviourFactory;
import dev.isxander.controlify.api.screenop.ScreenBehaviourRegistry;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.api.screenop.ScreenopEvents;
import dev.isxander.controlify.impl.screenop.behaviours.screen.*;
import dev.isxander.controlify.impl.screenop.behaviours.virtualmouse.*;
import dev.isxander.controlify.impl.utils.event.EventBus;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Objects;

public final class ScreenBehaviourRegistryImpl implements ScreenBehaviourRegistry {
	public static final class Events {
		public static final EventBus<ScreenBehaviourRegistry> REGISTER_SCREEN_BEHAVIOURS = new EventBus<>();
		public static final EventBus<ScreenopEvents.RegisterElementBehavioursEvent> REGISTER_ELEMENT_BEHAVIOURS = new EventBus<>();

		private Events() {
		}
	}

	public static final ScreenBehaviourRegistryImpl INSTANCE = new ScreenBehaviourRegistryImpl();

	private final BehaviourRegistry<Screen, ScreenBehaviourFactory<?>> registry = new BehaviourRegistry<>();
	private boolean initialized;
	private boolean ready;

	private ScreenBehaviourRegistryImpl() {
	}

	public void initialize() {
		if (initialized) {
			throw new IllegalStateException("Screen behaviours have already been initialized");
		}
		initialized = true;
		try {
			Events.REGISTER_SCREEN_BEHAVIOURS.emit(this);
			ready = true;
		} finally {
			registry.lock();
		}
	}

	@Override
	public <T extends Screen> void register(Class<T> screenClass, int priority, ScreenBehaviourFactory<? super T> factory) {
		registry.register(screenClass, priority, factory);
	}

	public boolean isReady() {
		return ready;
	}

	List<ScreenBehaviour> create(ScreenContext<?> context) {
		if (!ready) {
			throw new IllegalStateException("Screen contexts are available after REGISTER_SCREEN_BEHAVIOURS completes");
		}
		return registry.factories(context.screen()).stream()
				.map(factory -> create(factory, context))
				.toList();
	}

	@SuppressWarnings("unchecked")
	private static <T extends Screen> ScreenBehaviour create(ScreenBehaviourFactory<T> factory, ScreenContext<?> context) {
		// The registry selected this factory using its registered screen class.
		return Objects.requireNonNull(factory.create((ScreenContext<? extends T>) context), "Screen behaviour factory returned null");
	}
}
