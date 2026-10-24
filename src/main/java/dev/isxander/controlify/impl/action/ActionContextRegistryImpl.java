/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionContextRegistry;
import dev.isxander.controlify.api.action.BuiltinActionContexts;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.impl.screenop.ScreenRuntime;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.impl.utils.event.EventBus;
import dev.isxander.controlify.api.screenop.VirtualMouseMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

public final class ActionContextRegistryImpl implements ActionContextRegistry {
	/// Subscriptions do not initialize the context registry singleton.
	public static final class Events {
		public static final EventBus<ActionContextRegistry> REGISTER_CONTEXTS = new EventBus<>();

		private Events() {
		}
	}

	public static final ActionContextRegistryImpl INSTANCE = new ActionContextRegistryImpl();

	private final Map<Identifier, ActionContext> contexts = new TreeMap<>();
	private boolean locked;

	private ActionContextRegistryImpl() {
	}

	@Override
	public @NonNull Identifier register(@NonNull Identifier id, @NonNull Function<Minecraft, Boolean> isApplicable) {
		if (this.locked) {
			throw new IllegalStateException("Action context registration has closed");
		}
		ActionContext context = new ActionContext(Objects.requireNonNull(id), Objects.requireNonNull(isApplicable));
		if (this.contexts.putIfAbsent(id, context) != null) {
			throw new IllegalArgumentException("Action context is already registered: " + id);
		}
		return id;
	}

	public void registerBuiltinContexts() {
		this.register(BuiltinActionContexts.UNKNOWN, mc -> true);
		this.register(BuiltinActionContexts.IN_GAME,
				mc -> MinecraftUtil.getScreen() == null && mc.level != null && mc.player != null);
		this.register(BuiltinActionContexts.ANY_SCREEN, mc -> MinecraftUtil.getScreen() != null);
		this.register(BuiltinActionContexts.REGULAR_SCREEN,
				mc -> MinecraftUtil.getScreen() != null
						&& !ScreenRuntime.virtualMouse().map(mouse -> mouse.isEnabled()).orElse(false));
		this.register(BuiltinActionContexts.CONTAINER,
				mc -> MinecraftUtil.getScreen() instanceof AbstractContainerScreen<?>);
		this.register(BuiltinActionContexts.V_MOUSE_CURSOR,
				mc -> MinecraftUtil.getScreen() != null
						&& ScreenRuntime.virtualMouse().map(mouse -> mouse.isEnabled()).orElse(false));
		this.register(BuiltinActionContexts.V_MOUSE_COMPAT,
				mc -> MinecraftUtil.getScreen() != null
						&& ScreenRuntime.virtualMouse().map(mouse -> mouse.isEnabled() && mouse.mode().emulatesClicks()).orElse(false));
		this.register(BuiltinActionContexts.RADIAL_MENU, mc -> MinecraftUtil.getScreen() instanceof RadialMenuScreen);
	}

	public Set<ActionContext> getApplicableContexts(Minecraft minecraft) {
		if (!this.locked) {
			throw new IllegalStateException("Action contexts must be registered before evaluating them");
		}
		Set<ActionContext> applicable = new TreeSet<>();
		for (ActionContext context : this.contexts.values()) {
			if (context.isApplicable().apply(minecraft)) {
				applicable.add(context);
			}
		}
		return Collections.unmodifiableSet(applicable);
	}

	public void lock() {
		this.locked = true;
	}
}
