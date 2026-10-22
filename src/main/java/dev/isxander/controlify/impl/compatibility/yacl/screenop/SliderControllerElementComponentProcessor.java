/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.yacl.screenop;

import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.compatibility.yacl.mixins.ControllerWidgetAccessor;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ComponentProcessor;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.yacl3.gui.controllers.slider.SliderControllerElement;

public class SliderControllerElementComponentProcessor implements ComponentProcessor {
	private final SliderControllerElement slider;

	public SliderControllerElementComponentProcessor(SliderControllerElement element) {
		this.slider = element;
	}

	@Override
	public boolean overrideControllerButtons(ScreenProcessor<?> screen, ControllerImpl controller) {
		var left = ControlifyActions.GUI_SECONDARY_NAVI_LEFT.on(controller).pulse().consumePulse();
		var right = ControlifyActions.GUI_SECONDARY_NAVI_RIGHT.on(controller).pulse().consumePulse();

		if (!((ControllerWidgetAccessor) slider).getControl().option().available()) {
			return false;
		}

		if (left) {
			slider.incrementValue(-1);
		} else if (right) {
			slider.incrementValue(1);
		} else {
			return false;
		}

		return true;
	}
}
