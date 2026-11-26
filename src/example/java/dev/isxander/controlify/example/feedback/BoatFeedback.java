/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.feedback;

import dev.isxander.controlify.api.feedback.Band;
import dev.isxander.controlify.api.feedback.FeedbackEvent;
import dev.isxander.controlify.api.feedback.FeedbackEvents;
import dev.isxander.controlify.api.feedback.Site;

import static dev.isxander.controlify.example.ExampleMod.id;

/// What the player feels while boating. The boost has no event: it plays from an ambient rule
/// using the fact [dev.isxander.controlify.example.contextual.BoatContext] contributes.
public final class BoatFeedback {
	// #region events
	public static final FeedbackEvent<Void> HORN = FeedbackEvent.builder(id("horn")).build();

	public record Engine(float throttle) {
	}

	public static final FeedbackEvent<Engine> ENGINE = FeedbackEvent.builder(id("engine"))
			.param("throttle", Engine::throttle)
			.build();
	// #endregion events

	public static void registerEvents() {
		// #region register
		FeedbackEvents.REGISTER.subscribeForever(registry -> {
			registry.registerEvent(HORN);
			registry.registerEvent(ENGINE);
		});
		// #endregion register

		// #region code-effect
		// The engine's tone rises in pitch with the throttle. A data track's `hz` is fixed, so this needs code.
		FeedbackEvents.REGISTER.subscribeForever(registry -> registry.registerEffect(ENGINE, (playback, out) -> {
			if (playback.released()) {
				return false;
			}
			float throttle = playback.params().throttle();
			out.voice(Site.BODY, 40f + 80f * throttle, 0.3f * throttle);
			out.band(Site.BODY, Band.LOW, 0.1f * throttle);
			return true;
		}));
		// #endregion code-effect
	}

	private BoatFeedback() {
	}
}
