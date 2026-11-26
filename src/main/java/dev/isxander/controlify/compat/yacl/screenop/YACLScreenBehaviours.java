/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compat.yacl.screenop;

import dev.isxander.controlify.api.screenop.ElementBehaviours;
import dev.isxander.controlify.api.screenop.ScreenopEvents;
import dev.isxander.controlify.compat.yacl.mixins.ControllerWidgetAccessor;
import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingControllerElement;
import dev.isxander.yacl3.gui.controllers.slider.SliderControllerElement;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;

public final class YACLScreenBehaviours {
	private YACLScreenBehaviours() {
	}

	public static void initialize() {
		ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS.subscribeForever(registry ->
				registry.<YACLScreen>register(YACLScreen.class, YACLScreenBehaviour::new)
		);
		ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribeForever(event -> {
			var registry = event.registry();
			registry.register(OptionListWidget.class, OptionListWidgetBehaviour::new);
			registry.register(CyclingControllerElement.class, element -> ElementBehaviours.conditional(
					() -> ((ControllerWidgetAccessor) element).getControl().option().available(),
					ElementBehaviours.cycle(CyclingControllerElement::cycleValue).create(element)
			));
			registry.register(SliderControllerElement.class, element -> ElementBehaviours.conditional(
					() -> ((ControllerWidgetAccessor) element).getControl().option().available(),
					ElementBehaviours.secondarySlider(SliderControllerElement::incrementValue).create(element)
			));
			registry.register(StringControllerElement.class, StringControllerElementBehaviour::new);
		});
	}
}
