/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.action.ActionSpec;
import dev.isxander.controlify.api.action.ActionSpecRegistry;
import dev.isxander.controlify.impl.utils.event.EventBus;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ActionSpecRegistryImpl implements ActionSpecRegistry {
	/// Addons can subscribe without initializing the registry singleton.
	public static final class Events {
		public static final EventBus<ActionSpecRegistry> REGISTER_ACTIONS = new EventBus<>();

		private Events() {
		}
	}

	public static final ActionSpecRegistryImpl INSTANCE = new ActionSpecRegistryImpl();

	private final List<ActionSpec> registeredSpecs = new ArrayList<>();
	private boolean locked;

	@Override
	public @NonNull ActionAccessor register(@NonNull ActionSpec actionSpec) {
		this.checkLocked(false);
		if (this.registeredSpecs.stream().anyMatch(spec -> spec.id().equals(actionSpec.id()))) {
			throw new IllegalArgumentException("Action is already registered: " + actionSpec.id());
		}
		this.registeredSpecs.add(actionSpec);
		return new ActionAccessorImpl(actionSpec.id());
	}

	public Optional<ActionSpec> getSpec(Identifier actionId) {
		return this.registeredSpecs.stream().filter(spec -> spec.id().equals(actionId)).findAny();
	}

	public List<ActionSpec> getActionSpecs() {
		this.checkLocked(true);
		return Collections.unmodifiableList(this.registeredSpecs);
	}

	public void lock() {
		this.locked = true;
	}

	private void checkLocked(boolean required) {
		if (required) {
			if (!this.locked) {
				throw new IllegalStateException("Registry must be locked to perform this operation.");
			}
		} else {
			if (this.locked) {
				throw new IllegalStateException("Registry must be unlocked to perform this operation.");
			}
		}
	}
}
