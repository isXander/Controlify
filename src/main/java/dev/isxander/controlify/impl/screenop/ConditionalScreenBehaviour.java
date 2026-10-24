/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/// Retains a stateful behaviour while deciding whether it participates in each call.
/// Lifetime notifications always reach the behaviour, including deactivation.
public final class ConditionalScreenBehaviour implements ScreenBehaviour {
	private final BooleanSupplier enabled;
	private final ScreenBehaviour behaviour;

	public ConditionalScreenBehaviour(BooleanSupplier enabled, ScreenBehaviour behaviour) {
		this.enabled = enabled;
		this.behaviour = behaviour;
	}

	@Override
	public void handleNavigation(Controller controller, Runnable next) {
		if (enabled.getAsBoolean()) {
			behaviour.handleNavigation(controller, next);
		} else {
			next.run();
		}
	}

	@Override
	public void handleInteraction(Controller controller, Runnable next) {
		if (enabled.getAsBoolean()) {
			behaviour.handleInteraction(controller, next);
		} else {
			next.run();
		}
	}

	@Override
	public void onWidgetRebuild(Runnable next) {
		if (enabled.getAsBoolean()) {
			behaviour.onWidgetRebuild(next);
		} else {
			next.run();
		}
	}

	@Override
	public void update(float delta, Runnable next) {
		if (enabled.getAsBoolean()) {
			behaviour.update(delta, next);
		} else {
			next.run();
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta, Controller controller, Runnable next) {
		if (enabled.getAsBoolean()) {
			behaviour.extractRenderState(graphics, tickDelta, controller, next);
		} else {
			next.run();
		}
	}

	@Override
	public void collectSnapPoints(Consumer<SnapPoint> output, Runnable next) {
		if (enabled.getAsBoolean()) {
			behaviour.collectSnapPoints(output, next);
		} else {
			next.run();
		}
	}

	@Override
	public void onScreenActiveChanged(boolean active) {
		behaviour.onScreenActiveChanged(active);
	}
}
