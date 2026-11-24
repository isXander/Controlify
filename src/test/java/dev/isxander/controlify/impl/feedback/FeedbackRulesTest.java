/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.impl.contextual.ContextualPredicate;
import dev.isxander.controlify.impl.contextual.FeedbackRule;
import dev.isxander.controlify.impl.feedback.FeedbackTestSupport.RecordingRenderer;
import dev.isxander.controlify.impl.feedback.FeedbackTestSupport.TestServices;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.isxander.controlify.impl.feedback.FeedbackTestSupport.FRAME;
import static dev.isxander.controlify.impl.feedback.FeedbackTestSupport.constant;
import static dev.isxander.controlify.impl.feedback.FeedbackTestSupport.event;
import static dev.isxander.controlify.impl.feedback.FeedbackTestSupport.id;
import static dev.isxander.controlify.impl.feedback.FeedbackTestSupport.run;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/// How rules choose effects, and how the engine plays several effects for one event.
class FeedbackRulesTest {
	private static final Identifier EVENT = id("event");
	private static final Identifier A = id("a");
	private static final Identifier B = id("b");

	private static FeedbackRule rule(Identifier event, Identifier effect) {
		return new FeedbackRule(event, new ContextualPredicate.Static(true), effect);
	}

	// --- selection ---
	// Rules are matched by the rule engine, which only runs in the game. These cover what
	// happens to the rules it matched.

	@Test
	void everyMatchedRulePlays() {
		assertEquals(List.of(A, B), FeedbackSelection.effects(EVENT, List.of(rule(EVENT, A), rule(EVENT, B))));
	}


	@Test
	void noneStopsTheRulesBelowIt() {
		FeedbackRule stop = rule(EVENT, FeedbackRegistryImpl.NONE);

		assertEquals(List.of(A), FeedbackSelection.effects(EVENT, List.of(rule(EVENT, A), stop, rule(EVENT, B))));
		assertEquals(List.of(), FeedbackSelection.effects(EVENT, List.of(stop, rule(EVENT, B))));
	}


	// --- engine ---

	private final TestServices services = new TestServices();
	private final RecordingRenderer renderer = new RecordingRenderer();
	private final FeedbackEngine engine = new FeedbackEngine(this.services, List.of(this.renderer));
	private final FeedbackEventImpl<Void> event = event("event");

	private void holdFor(float seconds) {
		int frames = Math.round(seconds / FRAME);
		for (int i = 0; i < frames; i++) {
			this.engine.hold(this.event, null, null);
			this.engine.update(FRAME, false);
		}
	}


	@Test
	void effectThatStopsBeingSelectedEndsWhileTheOthersCarryOn() {
		this.services.effects.put(A, constant(0.5f, 0f));
		this.services.effects.put(B, constant(0f, 0.25f));
		this.services.selections.put(EVENT, List.of(A, B));
		this.holdFor(0.1f);

		this.services.selections.put(EVENT, List.of(B));
		this.holdFor(0.1f);

		assertEquals(0f, this.renderer.last().low(), 1e-6f);
		assertEquals(0.25f, this.renderer.last().high(), 1e-6f);
	}

	@Test
	void effectStartsAgainWhenItIsSelectedAgain() {
		// felt for the first 0.05 s of each time it is selected
		this.services.effects.put(A, (playback, out) -> {
			if (playback.released() || playback.seconds() >= 0.05f) {
				return false;
			}
			out.rumble(1f, 0f);
			return true;
		});
		this.services.selections.put(EVENT, List.of(A));

		this.holdFor(0.2f);
		assertEquals(0f, this.renderer.last().low(), 1e-6f, "ended by itself and was not restarted by holding");

		this.services.selections.put(EVENT, List.of());
		this.holdFor(0.1f);
		this.services.selections.put(EVENT, List.of(A));
		this.holdFor(FRAME);

		assertEquals(1f, this.renderer.last().low(), 1e-6f);
	}

	@Test
	void firedEventPlaysEverySelectedEffect() {
		this.services.effects.put(A, (playback, out) -> {
			out.rumble(0.5f, 0f);
			return playback.seconds() < 0.1f;
		});
		this.services.effects.put(B, (playback, out) -> {
			out.rumble(0f, 0.25f);
			return playback.seconds() < 0.1f;
		});
		this.services.selections.put(EVENT, List.of(A, B));

		this.engine.fire(this.event, null, null);
		this.engine.update(FRAME, false);

		assertEquals(0.5f, this.renderer.last().low(), 1e-6f);
		assertEquals(0.25f, this.renderer.last().high(), 1e-6f);
	}

	@Test
	void releaseEndsEveryHeldEffect() {
		this.services.effects.put(A, constant(0.5f, 0f));
		this.services.effects.put(B, constant(0f, 0.25f));
		this.services.selections.put(EVENT, List.of(A, B));
		this.holdFor(0.1f);

		this.engine.release(this.event);
		run(this.engine, 0.05f);

		assertEquals(0f, this.renderer.last().low(), 1e-6f);
		assertEquals(0f, this.renderer.last().high(), 1e-6f);
		assertFalse(this.engine.isPlaying());
	}

}
