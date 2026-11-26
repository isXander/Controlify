/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.feedback;

import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.feedback.FeedbackRegistryImpl;

public final class FeedbackEvents {
	/// Fires once during early setup, after Controlify's built-in events and effects are
	/// registered. Subscribe in the Controlify entrypoint. The registry locks afterwards.
	public static final EventSource<FeedbackRegistry> REGISTER = FeedbackRegistryImpl.Events.REGISTER;

	private FeedbackEvents() {
	}
}
