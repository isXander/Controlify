/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.contextual.ContextualStateSink;
import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.impl.feedback.data.DataFeedbackEffect;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/// The mixer. One per controller, updated once per rendered frame.
///
/// Every frame it samples every active playback, mixes the results, applies the user's gain
/// and hands the mixed frame to each renderer.
public final class FeedbackEngine {
	/// A held playback is released when its event has not been held for this long. Emit
	/// sites call `hold` once per game tick (50 ms), so this tolerates two missed ticks.
	public static final float HOLD_TIMEOUT_SECONDS = 0.15f;
	/// Frames longer than this advance the clock by only this much, so one long frame
	/// cannot time out every hold or skip most of a short effect.
	public static final float MAX_STEP_SECONDS = 0.05f;

	private final FeedbackServices services;
	private final List<FeedbackRenderer> renderers;

	private final List<PlaybackImpl> playbacks = new ArrayList<>();
	/// Keyed by event id for events, and by [StateHold] for state-driven rules, so that an
	/// event and an effect with the same id never share a hold.
	private final Map<Object, PlaybackImpl> holds = new HashMap<>();

	private final MixedFrame scratch = new MixedFrame();
	private final MixedFrame mixed = new MixedFrame();

	private boolean silent;

	public FeedbackEngine(FeedbackServices services, List<? extends FeedbackRenderer> renderers) {
		this.services = services;
		this.renderers = List.copyOf(renderers);
	}

	/// Plays an effect directly, without an event. Used for effects that are built at
	/// runtime, such as vibrations sent by a server.
	///
	/// @param id     identifies what is playing, for gain lookup
	/// @param source the gain source to use if no resource pack assigns `id` one
	/// @param pos    if not null, the playback is attenuated by distance from the camera to it
	/// @param range  the falloff range in blocks, ignored without `pos`
	public PlaybackImpl play(
			Identifier id,
			FeedbackSource source,
			FeedbackEffect<Object> effect,
			@Nullable Supplier<Vec3> pos,
			float range
	) {
		PlaybackImpl playback = new PlaybackImpl(id, id, effect, source, false, List.of(), pos, range);
		this.playbacks.add(playback);
		return playback;
	}

	public <P> void fire(
			FeedbackEventImpl<P> event,
			@Nullable P params,
			@Nullable Vec3 pos,
			@Nullable Consumer<ContextualStateSink> subject
	) {
		Resolved resolved = this.resolve(event, subject);
		if (resolved == null) {
			return;
		}
		if (resolved.effect() instanceof DataFeedbackEffect data && data.holdOnly()) {
			CUtil.LOGGER.warn(
					"Feedback effect '{}' never ends on its own, so event '{}' cannot fire it. Add an envelope or a duration.",
					resolved.id(), event.id()
			);
			return;
		}
		if (pos != null && this.services.falloff(pos, event.falloffRange()) <= 0f) {
			return;
		}

		PlaybackImpl playback = this.createPlayback(event, resolved, false, pos);
		this.setParams(event, playback, params);
		this.playbacks.add(playback);
	}

	public <P> void hold(
			FeedbackEventImpl<P> event,
			@Nullable P params,
			@Nullable Vec3 pos,
			@Nullable Consumer<ContextualStateSink> subject
	) {
		Resolved resolved = this.resolve(event, subject);
		PlaybackImpl current = this.holds.get(event.id());

		if (current != null && (resolved == null || !current.effectId.equals(resolved.id()))) {
			// the selected effect changed: the old one plays out its release
			current.release();
			this.holds.remove(event.id());
			current = null;
		}
		if (current == null) {
			if (resolved == null) {
				return;
			}
			current = this.createPlayback(event, resolved, true, pos);
			this.playbacks.add(current);
			this.holds.put(event.id(), current);
		}

		this.setParams(event, current, params);
		current.pos = pos == null ? null : () -> pos;
		current.idleSeconds = 0f;
	}

	/// Holds an effect with no event, for as long as this is called. Used by state-driven
	/// rules, which call it every tick their predicate is true.
	public void hold(Identifier effectId) {
		if (FeedbackRegistryImpl.NONE.equals(effectId)) {
			return;
		}

		StateHold key = new StateHold(effectId);
		PlaybackImpl current = this.holds.get(key);
		if (current == null) {
			FeedbackEffect<Object> effect = this.services.findEffect(effectId, effectId);
			if (effect == null) {
				return;
			}
			current = new PlaybackImpl(effectId, effectId, effect, FeedbackSource.MASTER, true, List.of(), null, 0f);
			this.playbacks.add(current);
			this.holds.put(key, current);
		}
		current.idleSeconds = 0f;
	}

	/// Ends a hold immediately instead of waiting for it to time out.
	public void release(FeedbackEventImpl<?> event) {
		PlaybackImpl playback = this.holds.remove(event.id());
		if (playback != null) {
			playback.release();
		}
	}

	private <P> void setParams(FeedbackEventImpl<P> event, PlaybackImpl playback, @Nullable P params) {
		playback.params = params;
		event.extractParams(params, playback.paramValues());
	}

	private PlaybackImpl createPlayback(FeedbackEventImpl<?> event, Resolved resolved, boolean held, @Nullable Vec3 pos) {
		return new PlaybackImpl(
				event.id(), resolved.id(), resolved.effect(), FeedbackSource.MASTER, held,
				event.paramNames(), pos == null ? null : () -> pos, event.falloffRange()
		);
	}

	/// Resolution order: the first matching selection rule, else the effect registered under
	/// the event's own id, else nothing. `controlify:none` explicitly plays nothing.
	private @Nullable Resolved resolve(FeedbackEventImpl<?> event, @Nullable Consumer<ContextualStateSink> subject) {
		Identifier effectId = null;
		if (event.usesSelection()) {
			effectId = this.services.select(event, subject);
		}
		if (effectId == null) {
			effectId = event.id();
		}
		if (FeedbackRegistryImpl.NONE.equals(effectId)) {
			return null;
		}

		FeedbackEffect<Object> effect = this.services.findEffect(effectId, event.id());
		return effect == null ? null : new Resolved(effectId, effect);
	}

	/// Samples all playbacks once and renders the result. Called once per rendered frame.
	///
	/// @param dt     seconds since the previous call
	/// @param paused whether the game is paused. Held playbacks freeze while it is, since the
	///               emit sites that keep them alive are not ticking; one-shots carry on.
	public void update(float dt, boolean paused) {
		dt = Math.clamp(dt, 0f, MAX_STEP_SECONDS);

		this.playbacks.removeIf(PlaybackImpl::isFinished);

		if (!paused) {
			final float step = dt;
			this.holds.values().removeIf(playback -> {
				// a playback that ended by itself stays here until its event stops being held,
				// so that holding does not restart it
				playback.idleSeconds += step;
				if (playback.idleSeconds > HOLD_TIMEOUT_SECONDS) {
					playback.release();
					return true;
				}
				return false;
			});
		}

		this.mixed.reset(this.services.hd());
		// playbacks are only ever added by emit sites, never while sampling
		for (PlaybackImpl playback : this.playbacks) {
			if (paused && playback.held && !playback.released()) {
				continue;
			}
			playback.sample(this.services, this.scratch, this.mixed, dt);
		}

		if (this.silent || !this.services.enabled()) {
			this.silenceRenderers();
		} else {
			for (FeedbackRenderer renderer : this.renderers) {
				renderer.render(this.mixed, dt);
			}
		}
	}

	/// While silent, effects keep playing but no renderer outputs anything.
	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public boolean isSilent() {
		return this.silent;
	}

	/// Stops everything immediately and silences all renderers.
	public void clear() {
		for (PlaybackImpl playback : this.playbacks) {
			playback.stop();
		}
		this.playbacks.clear();
		this.holds.clear();
		this.silenceRenderers();
	}

	public boolean isPlaying() {
		return !this.playbacks.isEmpty();
	}

	private void silenceRenderers() {
		for (FeedbackRenderer renderer : this.renderers) {
			renderer.silence();
		}
	}

	private record Resolved(Identifier id, FeedbackEffect<Object> effect) {
	}

	private record StateHold(Identifier effectId) {
	}
}
