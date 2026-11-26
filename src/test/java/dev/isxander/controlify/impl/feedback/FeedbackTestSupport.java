/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.Band;
import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.api.feedback.FeedbackEvent;
import dev.isxander.controlify.api.feedback.Site;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Shared fixtures for feedback tests, which run without a Minecraft client.
final class FeedbackTestSupport {
	/// One rendered frame at 60 fps.
	static final float FRAME = 1f / 60f;

	private FeedbackTestSupport() {
	}

	static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("controlify_test", path);
	}

	@SuppressWarnings("unchecked")
	static <P> FeedbackEventImpl<P> impl(FeedbackEvent<P> event) {
		return (FeedbackEventImpl<P>) event;
	}

	static FeedbackEventImpl<Void> event(String path) {
		return impl(FeedbackEvent.builder(id(path)).build());
	}

	/// An effect that plays a constant rumble until released.
	static FeedbackEffect<Object> constant(float low, float high) {
		return (playback, out) -> {
			if (playback.released()) {
				return false;
			}
			out.rumble(low, high);
			return true;
		};
	}

	/// Runs the engine for `seconds` of 60 fps frames.
	static void run(FeedbackEngine engine, float seconds) {
		int frames = Math.round(seconds / FRAME);
		for (int i = 0; i < frames; i++) {
			engine.update(FRAME, false);
		}
	}

	/// Services with every input controllable by the test.
	static final class TestServices implements FeedbackServices {
		/// Effects by id, playable for any event.
		final Map<Identifier, FeedbackEffect<Object>> effects = new HashMap<>();
		/// Gain by effect id.
		final Map<Identifier, Float> gains = new HashMap<>();
		/// Group rank by effect id.
		final Map<Identifier, Integer> ranks = new HashMap<>();
		/// Maps an event id to the effect ids its rules select; absent means it plays its own id.
		final Map<Identifier, List<Identifier>> selections = new HashMap<>();

		@Override
		public Mixing mixing(Identifier effectId, FeedbackGroup fallback) {
			return new Mixing(this.gains.getOrDefault(effectId, 1f), this.ranks.getOrDefault(effectId, 0));
		}

		@Override
		public boolean enabled() {
			return true;
		}

		@Override
		public boolean hd() {
			return false;
		}

		@Override
		public float falloff(Vec3 pos, float range) {
			return 1f;
		}

		@Override
		public List<Identifier> select(FeedbackEventImpl<?> event) {
			return this.selections.getOrDefault(event.id(), List.of(event.id()));
		}

		@Override
		public @Nullable FeedbackEffect<Object> findEffect(Identifier effectId, Identifier eventId) {
			return this.effects.get(effectId);
		}
	}

	/// Records the body bands of every frame handed to it.
	static final class RecordingRenderer implements FeedbackRenderer {
		record Frame(float low, float high) {
		}

		final List<Frame> frames = new ArrayList<>();

		@Override
		public void render(MixedFrame frame, float dt) {
			this.frames.add(new Frame(frame.band(Site.BODY, Band.LOW), frame.band(Site.BODY, Band.HIGH)));
		}

		@Override
		public void silence() {
			this.frames.add(new Frame(0f, 0f));
		}

		Frame last() {
			return this.frames.getLast();
		}
	}
}
