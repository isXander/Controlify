/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.compat;

import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ComponentProcessor;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;

/**
 * A component processor that handles incrementing and decrementing a slider.
 * This uses {@link dev.isxander.controlify.impl.action.ControlifyActions#GUI_SECONDARY_NAVI_RIGHT} and {@link dev.isxander.controlify.impl.action.ControlifyActions#GUI_SECONDARY_NAVI_LEFT} to increment and decrement the slider.
 */
public abstract class AbstractSliderComponentProcessor implements ComponentProcessor {

	@Override
	public boolean overrideControllerNavigation(ScreenProcessor<?> screen, ControllerImpl controller) {
		var left = ControlifyActions.GUI_SECONDARY_NAVI_LEFT.on(controller).pulse().consumePulse();
		var right = ControlifyActions.GUI_SECONDARY_NAVI_RIGHT.on(controller).pulse().consumePulse();

		if (left) {
			incrementSlider(true);
		} else if (right) {
			incrementSlider(false);
		} else {
			return false;
		}

		return true;
	}

	protected abstract void incrementSlider(boolean reverse);
}
