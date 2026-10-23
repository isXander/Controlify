/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ElementBehaviourFactory;
import dev.isxander.controlify.api.screenop.ElementBehaviourRegistry;
import dev.isxander.controlify.impl.screenop.behaviours.element.*;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;
import java.util.Objects;

final class ElementBehaviourRegistryImpl implements ElementBehaviourRegistry {
	private final BehaviourRegistry<GuiEventListener, ElementBehaviourFactory<?>> registry = new BehaviourRegistry<>();

	@Override
	public <T extends GuiEventListener> void register(Class<T> elementClass, int priority, ElementBehaviourFactory<? super T> factory) {
		registry.register(elementClass, priority, factory);
	}

	List<ElementBehaviour> create(GuiEventListener element) {
		return registry.factories(element).stream().map(factory -> create(factory, element)).toList();
	}

	@SuppressWarnings("unchecked")
	private static <T extends GuiEventListener> ElementBehaviour create(ElementBehaviourFactory<T> factory, GuiEventListener element) {
		return Objects.requireNonNull(factory.create((T) element), "Element behaviour factory returned null");
	}

	void lock() {
		registry.lock();
	}
}
