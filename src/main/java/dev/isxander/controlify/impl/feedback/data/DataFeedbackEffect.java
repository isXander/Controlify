/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.api.feedback.FeedbackFrame;
import dev.isxander.controlify.api.feedback.Playback;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/// An effect loaded from `assets/<namespace>/controller/feedback/<path>.json`. It works with
/// any event, reading the event's named params.
///
/// When fired, it plays until its longest track ends (or `duration`, if given). When held,
/// it plays until released, each envelope holding its final strength once it runs out; it
/// then fades out over `release` seconds.
///
/// @param duration seconds after which the effect ends, overriding the length of its tracks
/// @param release  seconds the effect takes to fade out after a hold is released
public record DataFeedbackEffect(
		List<Track> tracks,
		Optional<Float> duration,
		float release
) implements FeedbackEffect<Object> {
	public static final Codec<DataFeedbackEffect> CODEC = RecordCodecBuilder.<DataFeedbackEffect>create(instance -> instance.group(
			Track.CODEC.listOf().fieldOf("tracks").forGetter(DataFeedbackEffect::tracks),
			Codec.FLOAT.optionalFieldOf("duration").forGetter(DataFeedbackEffect::duration),
			Codec.FLOAT.optionalFieldOf("release", 0f).forGetter(DataFeedbackEffect::release)
	).apply(instance, DataFeedbackEffect::new)).validate(effect -> {
		if (effect.tracks.isEmpty()) {
			return DataResult.error(() -> "A feedback effect needs at least one track");
		}
		if (effect.duration.isPresent() && !(effect.duration.get() > 0f && Float.isFinite(effect.duration.get()))) {
			return DataResult.error(() -> "A feedback effect's 'duration' must be positive");
		}
		if (!Float.isFinite(effect.release) || effect.release < 0f) {
			return DataResult.error(() -> "A feedback effect's 'release' cannot be negative");
		}
		return DataResult.success(effect);
	});

	public DataFeedbackEffect {
		tracks = List.copyOf(tracks);
	}

	/// The time in seconds this effect ends by itself when fired, or infinity if none of its
	/// tracks has a length and no `duration` is given.
	public float end() {
		if (this.duration.isPresent()) {
			return this.duration.get();
		}
		float end = Float.NEGATIVE_INFINITY;
		for (Track track : this.tracks) {
			end = Math.max(end, track.end().orElse(Float.NEGATIVE_INFINITY));
		}
		return end == Float.NEGATIVE_INFINITY ? Float.POSITIVE_INFINITY : end;
	}

	/// Whether this effect never ends by itself, so it only makes sense for held events.
	public boolean holdOnly() {
		return Float.isInfinite(this.end());
	}

	@Override
	public boolean sample(@NonNull Playback<Object> playback, @NonNull FeedbackFrame out) {
		float fade = 1f;
		if (playback.released()) {
			if (playback.sinceRelease() >= this.release) {
				return false;
			}
			fade = 1f - playback.sinceRelease() / this.release;
		} else if (!playback.held() || this.duration.isPresent()) {
			// the sample that reaches the end still plays, so a click at the very end is not lost
			if (playback.previousSeconds() >= this.end()) {
				return false;
			}
		}

		for (Track track : this.tracks) {
			track.write(playback, out, fade);
		}
		return true;
	}
}
