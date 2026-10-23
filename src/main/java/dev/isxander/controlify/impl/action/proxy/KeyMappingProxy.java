/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.proxy;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.KeyMappingHandle;
import dev.isxander.controlify.impl.action.ActionImpl;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/// Consumes an action's pulses and forwards its held state to explicitly registered key mappings.
final class KeyMappingProxy implements EventSink<ControllerImpl> {
	private final ActionAccessor action;
	private final Map<KeyMapping, Target> keyMappings = new LinkedHashMap<>();
	private @Nullable ControllerImpl owner;
	private @Nullable Action ownedAction;
	private long actionResetVersion;

	KeyMappingProxy(ActionAccessor action) {
		this.action = action;
	}

	void addKeyMapping(KeyMapping keyMapping, Predicate<Controller> toggleCondition) {
		this.keyMappings.put(keyMapping, new Target(toggleCondition, new KeyMappingProxyState()));
	}

	@Override
	public void accept(@NonNull ControllerImpl controller) {
		Action resolvedAction = this.action.onOrNull(controller);
		Minecraft minecraft = Minecraft.getInstance();
		boolean enabled = controller.isActive()
				&& Controlify.instance().currentInputMode().isController()
				&& (minecraft.isWindowActive() || Controlify.instance().config().getSettings().globalSettings().outOfFocusInput);
		if (!enabled || resolvedAction == null) {
			if (resolvedAction != null) {
				resolvedAction.pulse().consumeAllPulses();
			}
			this.release(controller);
			return;
		}
		long resetVersion = ((ActionImpl) resolvedAction).state().resetVersion();
		if (this.owner != null && (this.owner != controller || this.ownedAction != resolvedAction
				|| this.actionResetVersion != resetVersion)) {
			this.release(this.owner);
		}
		this.owner = controller;
		this.ownedAction = resolvedAction;
		this.actionResetVersion = resetVersion;

		int pulses = 0;
		while (resolvedAction.pulse().consumePulse()) {
			pulses++;
			for (KeyMapping keyMapping : this.keyMappings.keySet()) {
				((KeyMappingHandle) keyMapping).controlify$click();
			}
		}
		for (var entry : this.keyMappings.entrySet()) {
			var state = entry.getValue().state();
			boolean wasDown = state.isDown();
			state.update(entry.getValue().toggleCondition().test(controller), resolvedAction.latch().isLatchActive(), pulses);
			if (wasDown || state.isDown()) {
				((KeyMappingHandle) entry.getKey()).controlify$setDown(state.isDown());
			}
		}
	}

	void release(ControllerImpl controller) {
		if (this.owner != controller) {
			return;
		}
		for (var entry : this.keyMappings.entrySet()) {
			if (entry.getValue().state().isDown()) {
				((KeyMappingHandle) entry.getKey()).controlify$setDown(false);
			}
			entry.getValue().state().reset();
		}
		this.owner = null;
		this.ownedAction = null;
	}

	private record Target(Predicate<Controller> toggleCondition, KeyMappingProxyState state) {
	}
}
