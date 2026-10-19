/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.yacl.screenop;

import dev.isxander.controlify.impl.bindings.ControlifyBindings;
import dev.isxander.controlify.impl.compatibility.yacl.mixins.ControllerWidgetAccessor;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ComponentProcessor;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingControllerElement;

public class CyclingControllerElementComponentProcessor implements ComponentProcessor {
	private final CyclingControllerElement cyclingController;
	private boolean prevLeft, prevRight;


	public CyclingControllerElementComponentProcessor(CyclingControllerElement cyclingController) {
		this.cyclingController = cyclingController;
	}

	@Override
	public boolean overrideControllerNavigation(ScreenProcessor<?> screen, ControllerImpl controller) {
		boolean left = ControlifyBindings.GUI_SECONDARY_NAVI_LEFT.on(controller).digitalNow();
		boolean right = ControlifyBindings.GUI_SECONDARY_NAVI_RIGHT.on(controller).digitalNow();

		if (!((ControllerWidgetAccessor) cyclingController).getControl().option().available()) {
			return false;
		}

		if (left && !prevLeft) {
			prevLeft = true;
			prevRight = false;

			cyclingController.cycleValue(-1);

			return true;
		} else if (right && !prevRight) {
			prevLeft = false;
			prevRight = true;

			cyclingController.cycleValue(1);

			return true;
		} else {
			prevLeft = left;
			prevRight = right;

			return false;
		}
	}
}
