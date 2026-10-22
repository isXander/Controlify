package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.screenop.ScreenBehaviourRegistryImpl;

public final class ScreenopEvents {

	/// Fires once during client initialization. Registrations close after dispatch.
	public static final EventSource<ScreenBehaviourRegistry> REGISTER_SCREEN_BEHAVIOURS = ScreenBehaviourRegistryImpl.Events.REGISTER_SCREEN_BEHAVIOURS;

	/// Fires once for each new screen context, after its screen factories have run.
	/// Register factories here; widgets may not have been initialized yet. The registry
	/// closes after dispatch and is retained across widget rebuilds.
	public static final EventSource<RegisterElementBehavioursEvent> REGISTER_ELEMENT_BEHAVIOURS = ScreenBehaviourRegistryImpl.Events.REGISTER_ELEMENT_BEHAVIOURS;

	public record RegisterElementBehavioursEvent(ScreenContext<?> screenContext, ElementBehaviourRegistry registry) {
	}

	private ScreenopEvents() {
	}
}
