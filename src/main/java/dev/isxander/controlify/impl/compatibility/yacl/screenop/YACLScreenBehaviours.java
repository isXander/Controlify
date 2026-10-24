/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.yacl.screenop;

import dev.isxander.controlify.api.screenop.ElementBehaviours;
import dev.isxander.controlify.api.screenop.ScreenopEvents;
import dev.isxander.controlify.impl.compatibility.yacl.mixins.ControllerWidgetAccessor;
import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingControllerElement;
import dev.isxander.yacl3.gui.controllers.slider.SliderControllerElement;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;

public final class YACLScreenBehaviours {
	private YACLScreenBehaviours() {
	}

	public static void initialize() {
		ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS.subscribe(registry -> registry.<YACLScreen>register(YACLScreen.class, YACLScreenBehaviour::new));
		ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribe(event -> {
			var registry = event.registry();
			registry.register(OptionListWidget.class, OptionListWidgetBehaviour::new);
			registry.register(CyclingControllerElement.class, element -> ElementBehaviours.conditional(
					() -> ((ControllerWidgetAccessor) element).getControl().option().available(),
					ElementBehaviours.<CyclingControllerElement>cycle((widget, direction) -> widget.cycleValue(direction)).create(element)
			));
			registry.register(SliderControllerElement.class, element -> ElementBehaviours.conditional(
					() -> ((ControllerWidgetAccessor) element).getControl().option().available(),
					ElementBehaviours.<SliderControllerElement>secondarySlider((widget, direction) -> widget.incrementValue(direction)).create(element)
			));
			registry.register(StringControllerElement.class, StringControllerElementBehaviour::new);
		});
	}
}
