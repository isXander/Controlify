package dev.isxander.controlify.impl.compatibility.yacl.screenop;

import dev.isxander.controlify.api.screenop.ScreenopEvents;
import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingControllerElement;
import dev.isxander.yacl3.gui.controllers.slider.SliderControllerElement;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;

public final class YACLScreenBehaviours {
	private YACLScreenBehaviours() {
	}

	public static void initialize() {
		ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS.subscribe(registry -> registry.registerUnchecked(YACLScreen.class));
		ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribe(event -> {
			var registry = event.registry();
			registry.registerUnchecked(OptionListWidget.class);
			registry.registerUnchecked(CyclingControllerElement.class);
			registry.registerUnchecked(SliderControllerElement.class);
			registry.registerUnchecked(StringControllerElement.class);
		});
	}
}
