/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.feedback;

import net.minecraft.resources.Identifier;

/// Where addons register their own events and code effects. See [FeedbackEvents#REGISTER].
///
/// Most effects should be data: a file at `assets/<namespace>/controller/feedback/<path>.json`
/// needs no registration and always wins over a code effect with the same id.
public interface FeedbackRegistry {
	/// Registers an event so it appears in diagnostics. Events work without registration.
	///
	/// @throws IllegalStateException if the event id is already registered
	void registerEvent(FeedbackEvent<?> event);

	/// Registers a code effect that plays for `event`. An event plays the effect with its own
	/// id unless a selection rule names another, so use the event's id for its default effect
	/// and other ids for the variants that rules select.
	///
	/// @throws IllegalStateException if a code effect already has that id; use [#replaceEffect]
	<P> void registerEffect(Identifier effectId, FeedbackEvent<P> event, FeedbackEffect<? super P> effect);

	/// Registers the default code effect of `event`.
	default <P> void registerEffect(FeedbackEvent<P> event, FeedbackEffect<? super P> effect) {
		this.registerEffect(event.id(), event, effect);
	}

	/// Like [#registerEffect], but replaces any code effect already registered with that id.
	<P> void replaceEffect(Identifier effectId, FeedbackEvent<P> event, FeedbackEffect<? super P> effect);
}
