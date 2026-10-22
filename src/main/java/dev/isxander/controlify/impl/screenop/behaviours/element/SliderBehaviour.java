/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.element;

import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.utils.MinecraftUtil;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public final class SliderBehaviour implements ElementBehaviour {
	private final IntConsumer adjust;
	private final BooleanSupplier isEditing;
	private final Consumer<Boolean> setEditing;
	private boolean editing;

	public SliderBehaviour(IntConsumer adjust) {
		this.adjust = adjust;
		this.isEditing = () -> editing;
		this.setEditing = editing -> this.editing = editing;
	}

	public SliderBehaviour(IntConsumer adjust, BooleanSupplier isEditing, Consumer<Boolean> setEditing) {
		this.adjust = adjust;
		this.isEditing = isEditing;
		this.setEditing = setEditing;
	}

	@Override
	public void handleNavigation(ScreenContext<?> screen, Controller controller, Runnable next) {
		if (screen.isVirtualMouseEnabled()) {
			next.run();
			return;
		}

		boolean handled = adjustPulses(screen, controller, ControlifyActions.GUI_SECONDARY_NAVI_LEFT, -1);
		handled |= adjustPulses(screen, controller, ControlifyActions.GUI_SECONDARY_NAVI_RIGHT, 1);
		if (isEditing.getAsBoolean()) {
			handled |= adjustPulses(screen, controller, ControlifyActions.GUI_NAVI_LEFT, -1);
			handled |= adjustPulses(screen, controller, ControlifyActions.GUI_NAVI_RIGHT, 1);
		}
		if (!handled) {
			next.run();
		}
	}

	private boolean adjustPulses(ScreenContext<?> screen, Controller controller, ActionAccessor action, int direction) {
		boolean handled = false;
		while (MinecraftUtil.getScreen() == screen.screen() && action.on(controller).pulse().consumePulse()) {
			adjust.accept(direction);
			screen.playNavigationHaptic(controller);
			handled = true;
		}
		return handled;
	}

	@Override
	public void handleInteraction(ScreenContext<?> screen, Controller controller, Runnable next) {
		if (screen.isVirtualMouseEnabled()) {
			next.run();
			return;
		}
		if (isEditing.getAsBoolean() && ControlifyActions.GUI_BACK.on(controller).pulse().consumeAllPulses()) {
			// Back wins if both were pressed, so Press cannot immediately reopen editing.
			ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses();
			setEditing.accept(false);
			return;
		}
		if (ControlifyActions.GUI_PRESS.on(controller).pulse().consumeAllPulses()) {
			setEditing.accept(!isEditing.getAsBoolean());
			screen.playNavigationHaptic(controller);
			return;
		}
		next.run();
	}

	@Override
	public void onFocusChanged(ScreenContext<?> screen, boolean focused) {
		setEditing.accept(false);
	}
}
