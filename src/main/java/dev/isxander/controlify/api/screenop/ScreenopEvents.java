/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.screenop.ScreenBehaviourRegistryImpl;

/// Registration events for screen and per-screen element behaviours.
/// Subscribe during client initialization before the corresponding registration dispatch.
/// Use explicit factories; screens and elements do not need to implement factory interfaces.
public final class ScreenopEvents {

	/// Fires once during client initialization to register factories by screen type.
	/// All matching registrations compose. The supplied registry closes when dispatch
	/// finishes; factories run later as individual screen contexts are created.
	public static final EventSource<ScreenBehaviourRegistry> REGISTER_SCREEN_BEHAVIOURS = ScreenBehaviourRegistryImpl.Events.REGISTER_SCREEN_BEHAVIOURS;

	/// Fires once for each new screen context, after its screen behaviour factories run.
	/// The event exposes the current context so registrations for the same element type can
	/// vary between screens. Widgets may not yet be initialized: register factories here
	/// and let them run lazily when elements are resolved.
	///
	/// The registry closes after dispatch and is retained across widget rebuilds. Do not
	/// resolve element behaviours through the context until this event has finished.
	public static final EventSource<RegisterElementBehavioursEvent> REGISTER_ELEMENT_BEHAVIOURS = ScreenBehaviourRegistryImpl.Events.REGISTER_ELEMENT_BEHAVIOURS;

	/// Arguments for per-screen element behaviour registration.
	/// Factories may capture `screenContext` to share the owning screen's services or select
	/// behaviours appropriate to that screen. Neither component represents a global registry.
	///
	/// @param screenContext the context whose element registrations are being collected
	/// @param registry the additive element registry, writable only during this event
	public record RegisterElementBehavioursEvent(ScreenContext<?> screenContext, ElementBehaviourRegistry registry) {
	}

	private ScreenopEvents() {
	}
}
