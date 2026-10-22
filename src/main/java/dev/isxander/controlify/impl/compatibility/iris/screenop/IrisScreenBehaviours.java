package dev.isxander.controlify.impl.compatibility.iris.screenop;

//? if iris {

import dev.isxander.controlify.api.screenop.ScreenopEvents;
import net.irisshaders.iris.gui.element.widget.BaseOptionElementWidget;

public final class IrisScreenBehaviours {
	private IrisScreenBehaviours() {
	}

	public static void initialize() {
		ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribe(event ->
				event.registry().registerUnchecked(BaseOptionElementWidget.class));
	}
}
//?}
