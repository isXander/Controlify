/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.proxy;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.utils.event.EventSink;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.KeyMappingHandle;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/// Consumes an action's pulses and forwards its held state to explicitly registered key mappings.
final class KeyMappingProxy implements EventSink<ControllerImpl> {
	private final ActionAccessor action;
	private final Set<KeyMapping> keyMappings = new LinkedHashSet<>();
	private @Nullable ControllerImpl owner;
	private boolean held;

	KeyMappingProxy(ActionAccessor action) {
		this.action = action;
	}

	void addKeyMapping(KeyMapping keyMapping) {
		this.keyMappings.add(keyMapping);
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
		if (this.owner != null && this.owner != controller) {
			this.release(this.owner);
		}
		this.owner = controller;

		while (resolvedAction.pulse().consumePulse()) {
			for (KeyMapping keyMapping : this.keyMappings) {
				((KeyMappingHandle) keyMapping).controlify$click();
			}
		}
		boolean down = resolvedAction.latch().isLatchActive();
		if (down || this.held) {
			for (KeyMapping keyMapping : this.keyMappings) {
				((KeyMappingHandle) keyMapping).controlify$setDown(down);
			}
		}
		this.held = down;
	}

	void release(ControllerImpl controller) {
		if (this.owner != controller) {
			return;
		}
		if (this.held) {
			for (KeyMapping keyMapping : this.keyMappings) {
				((KeyMappingHandle) keyMapping).controlify$setDown(false);
			}
		}
		this.owner = null;
		this.held = false;
	}
}
