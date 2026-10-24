/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.iris.screenop;

//? if iris {

import dev.isxander.controlify.api.screenop.ElementBehaviours;
import dev.isxander.controlify.api.screenop.ScreenopEvents;
import dev.isxander.controlify.impl.screenop.behaviours.element.ControlValueAccess;
import net.irisshaders.iris.gui.element.widget.BaseOptionElementWidget;

public final class IrisScreenBehaviours {
	private IrisScreenBehaviours() {
	}

	public static void initialize() {
		ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribe(event ->
				event.registry().register(BaseOptionElementWidget.class, ElementBehaviours.secondarySlider(
						(widget, direction) -> ((ControlValueAccess) widget).controlify$adjustValue(direction)
				)));
	}
}
//?}
