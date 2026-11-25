/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.api.feedback.Playback;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Supplier;

/// The engine's per-playback state. Effect definitions never hold any of this.
public final class PlaybackImpl implements Playback<Object> {
	final Identifier eventId;
	final Identifier effectId;
	final FeedbackEffect<Object> effect;
	final FeedbackGroup fallbackGroup;
	final boolean held;
	/// Increases with every playback an engine starts, so newer playbacks draw on top.
	final long sequence;
	final float range;

	private final List<String> paramNames;
	private final float[] paramValues;
	@Nullable Object params;
	@Nullable Supplier<Vec3> pos;
	/// Seconds since `hold` was last called for this playback.
	float idleSeconds;
	/// Refreshed every frame before sampling, since resources and settings can change.
	FeedbackServices.Mixing mixing = new FeedbackServices.Mixing(1f, 0);
	/// The groups to mix as instead of the ones resource packs give the effect, e.g. when a server picks them.
	@Nullable List<FeedbackGroup> groupsOverride;
	/// Whether this is still felt while the engine is game-silent. Refreshed with `mixing`.
	boolean feltWhilePaused;

	private float seconds;
	private float previousSeconds = -1f;
	private float releasedAt = -1f;
	private boolean finished;

	PlaybackImpl(
			Identifier eventId,
			Identifier effectId,
			FeedbackEffect<Object> effect,
			FeedbackGroup fallbackGroup,
			boolean held,
			long sequence,
			List<String> paramNames,
			@Nullable Supplier<Vec3> pos,
			float range
	) {
		this.eventId = eventId;
		this.effectId = effectId;
		this.effect = effect;
		this.fallbackGroup = fallbackGroup;
		this.held = held;
		this.sequence = sequence;
		this.paramNames = paramNames;
		this.paramValues = new float[paramNames.size()];
		this.pos = pos;
		this.range = range;
	}

	@Override
	public float seconds() {
		return this.seconds;
	}

	@Override
	public float previousSeconds() {
		return this.previousSeconds;
	}

	@Override
	public @Nullable Object params() {
		return this.params;
	}

	@Override
	public float param(@NonNull String name, float fallback) {
		int index = this.paramNames.indexOf(name);
		return index < 0 ? fallback : this.paramValues[index];
	}

	@Override
	public boolean held() {
		return this.held;
	}

	@Override
	public float sinceRelease() {
		return this.releasedAt < 0f ? -1f : this.seconds - this.releasedAt;
	}

	/// The named floats, in the order the event declared them.
	float[] paramValues() {
		return this.paramValues;
	}

	void release() {
		if (this.releasedAt < 0f) {
			this.releasedAt = this.seconds;
		}
	}

	/// Ends the playback immediately without a release tail.
	public void stop() {
		this.finished = true;
	}

	public boolean isFinished() {
		return this.finished;
	}

	/// Samples the effect once, mixes its output, then moves the clock on by `dt`.
	void sample(FeedbackServices services, MixedFrame scratch, MixedFrame mixed, float dt) {
		if (this.finished) {
			return;
		}

		float falloff = 1f;
		if (this.pos != null) {
			Vec3 at = this.pos.get();
			// a position that cannot be found any more, such as an entity that unloaded, ends the playback
			falloff = at == null ? -1f : services.falloff(at, this.range);
			if (falloff < 0f) {
				// there is nothing to measure the distance from, so the playback ends
				this.finished = true;
				return;
			}
		}

		scratch.reset(mixed.hd());
		boolean alive = this.effect.sample(this, scratch);
		this.previousSeconds = this.seconds;
		this.seconds += dt;
		if (!alive) {
			this.finished = true;
			return;
		}

		mixed.mix(scratch, this.mixing.gain() * falloff);
	}
}
