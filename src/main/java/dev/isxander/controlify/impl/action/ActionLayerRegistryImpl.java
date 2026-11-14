/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionLayerRegistry;
import dev.isxander.controlify.api.action.BuiltinActionLayers;
import dev.isxander.controlify.api.action.ActionLayerMode;
import dev.isxander.controlify.impl.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.impl.screenop.ScreenRuntime;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.impl.utils.event.EventBus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import dev.isxander.controlify.impl.utils.CUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public final class ActionLayerRegistryImpl implements ActionLayerRegistry {
	/// Subscriptions do not initialize the context registry singleton.
	public static final class Events {
		public static final EventBus<ActionLayerRegistry> REGISTER_LAYERS = new EventBus<>();

		private Events() {
		}
	}

	public static final ActionLayerRegistryImpl INSTANCE = new ActionLayerRegistryImpl();

	private final Map<Identifier, ActionLayer> contexts = new HashMap<>();
	private final Map<Identifier, LayerOrdering.Placement> placements = new HashMap<>();
	/// Top to bottom; populated when registration closes.
	private List<ActionLayer> orderedContexts = List.of();
	private boolean locked;

	private ActionLayerRegistryImpl() {
	}

	@Override
	public @NonNull Builder layer(@NonNull Identifier id) {
		return new BuilderImpl(Objects.requireNonNull(id));
	}

	public void registerBuiltinContexts() {
		// Radial actions shadow screen actions on the same inputs; screen-wide actions such as back still work.
		this.layer(BuiltinActionLayers.RADIAL_MENU)
				.mode(ActionLayerMode.OVERLAY)
				.activeWhen(mc -> MinecraftUtil.getScreen() instanceof RadialMenuScreen)
				.requires(BuiltinActionLayers.ANY_SCREEN)
				.above(BuiltinActionLayers.V_MOUSE_CURSOR)
				.register();
		// Screen sub-contexts overlap on purpose: which of their actions applies depends on what is
		// hovered or focused, so they consume nothing and the screen behaviours decide.
		this.layer(BuiltinActionLayers.V_MOUSE_CURSOR)
				.mode(ActionLayerMode.TRANSPARENT)
				.activeWhen(mc -> MinecraftUtil.getScreen() != null
						&& ScreenRuntime.virtualMouse().map(mouse -> mouse.isEnabled()).orElse(false))
				.requires(BuiltinActionLayers.ANY_SCREEN)
				.above(BuiltinActionLayers.CONTAINER)
				.register();
		this.layer(BuiltinActionLayers.CONTAINER)
				.mode(ActionLayerMode.TRANSPARENT)
				.activeWhen(mc -> MinecraftUtil.getScreen() instanceof AbstractContainerScreen<?>)
				.requires(BuiltinActionLayers.ANY_SCREEN)
				.above(BuiltinActionLayers.REGULAR_SCREEN)
				.register();
		this.layer(BuiltinActionLayers.REGULAR_SCREEN)
				.mode(ActionLayerMode.TRANSPARENT)
				.activeWhen(mc -> MinecraftUtil.getScreen() != null
						&& !ScreenRuntime.virtualMouse().map(mouse -> mouse.isEnabled()).orElse(false))
				.requires(BuiltinActionLayers.ANY_SCREEN)
				.above(BuiltinActionLayers.ANY_SCREEN)
				.register();
		// Any open screen hides gameplay.
		this.layer(BuiltinActionLayers.ANY_SCREEN)
				.mode(ActionLayerMode.BLOCKING)
				.activeWhen(mc -> MinecraftUtil.getScreen() != null)
				.above(BuiltinActionLayers.IN_GAME)
				.register();
		this.layer(BuiltinActionLayers.IN_GAME)
				.mode(ActionLayerMode.OVERLAY)
				.activeWhen(mc -> mc.level != null && mc.player != null)
				.below(BuiltinActionLayers.ANY_SCREEN)
				.register();
	}

	/// @return the active contexts, ordered from top to bottom
	public List<ActionLayer> getActiveContexts(Minecraft minecraft) {
		if (!this.locked) {
			throw new IllegalStateException("Action contexts must be registered before evaluating them");
		}
		List<ActionLayer> active = new ArrayList<>(this.orderedContexts.size());
		Map<Identifier, Boolean> evaluated = new HashMap<>();
		for (ActionLayer context : this.orderedContexts) {
			if (this.isActive(context, minecraft, evaluated)) {
				active.add(context);
			}
		}
		return active;
	}

	/// A layer is only active while every layer it requires is.
	private boolean isActive(ActionLayer context, Minecraft minecraft, Map<Identifier, Boolean> evaluated) {
		Boolean known = evaluated.get(context.id());
		if (known != null) {
			return known;
		}
		// Layers requiring each other in a cycle are never active.
		evaluated.put(context.id(), false);
		boolean active = context.isActive().test(minecraft);
		for (Identifier requiredId : context.requires()) {
			if (!active) {
				break;
			}
			ActionLayer required = this.contexts.get(requiredId);
			active = required == null || this.isActive(required, minecraft, evaluated);
		}
		evaluated.put(context.id(), active);
		return active;
	}

	/// @return every registered context, ordered from top to bottom
	public List<ActionLayer> getOrderedContexts() {
		return this.orderedContexts;
	}

	public void lock() {
		if (this.locked) {
			return;
		}
		this.locked = true;
		this.orderedContexts = LayerOrdering.sort(this.placements, CUtil.LOGGER::warn).stream()
				.map(this.contexts::get)
				.toList();
	}

	private final class BuilderImpl implements Builder {
		private final Identifier id;
		private ActionLayerMode mode = ActionLayerMode.OVERLAY;
		private @Nullable Predicate<Minecraft> isActive;
		private final Set<Identifier> requires = new LinkedHashSet<>();
		private final Set<Identifier> above = new LinkedHashSet<>();
		private final Set<Identifier> below = new LinkedHashSet<>();
		private boolean top, bottom;

		private BuilderImpl(Identifier id) {
			this.id = id;
		}

		@Override
		public @NonNull Builder mode(@NonNull ActionLayerMode mode) {
			this.mode = Objects.requireNonNull(mode);
			return this;
		}

		@Override
		public @NonNull Builder activeWhen(@NonNull Predicate<Minecraft> isActive) {
			this.isActive = Objects.requireNonNull(isActive);
			return this;
		}

		@Override
		public @NonNull Builder requires(@NonNull Identifier other) {
			this.requires.add(Objects.requireNonNull(other));
			return this;
		}

		@Override
		public @NonNull Builder above(@NonNull Identifier other) {
			this.above.add(Objects.requireNonNull(other));
			return this;
		}

		@Override
		public @NonNull Builder below(@NonNull Identifier other) {
			this.below.add(Objects.requireNonNull(other));
			return this;
		}

		@Override
		public @NonNull Builder top() {
			this.top = true;
			return this;
		}

		@Override
		public @NonNull Builder bottom() {
			this.bottom = true;
			return this;
		}

		@Override
		public @NonNull Identifier register() {
			ActionLayerRegistryImpl registry = ActionLayerRegistryImpl.this;
			if (registry.locked) {
				throw new IllegalStateException("Action context registration has closed");
			}
			if (this.isActive == null) {
				throw new IllegalStateException("Action context " + this.id + " needs a predicate");
			}
			var placement = new LayerOrdering.Placement(this.above, this.below, this.top, this.bottom);
			if (placement.isEmpty()) {
				throw new IllegalStateException("Action context " + this.id + " needs a placement relative to other contexts");
			}
			if (registry.contexts.containsKey(this.id)) {
				throw new IllegalArgumentException("Action context is already registered: " + this.id);
			}
			registry.contexts.put(this.id, new ActionLayer(this.id, this.mode, this.isActive, this.requires));
			registry.placements.put(this.id, placement);
			return this.id;
		}
	}
}
