/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.impl.feedback.data.DataFeedbackEffect;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/// The mixer. One per controller, updated once per rendered frame.
///
/// Every frame it samples every active playback, mixes the results, applies the user's gain
/// and hands the mixed frame to each renderer.
///
/// Playbacks are mixed in [#DRAW_ORDER], from lowest to highest priority, so renderers that
/// layer or drop outputs (light, limited voices) favour the higher-priority ones.
public final class FeedbackEngine {
	/// The event that ambient effects play for. Its playbacks draw below other held ones.
	static final Identifier AMBIENT_EVENT = Identifier.fromNamespaceAndPath("controlify", "ambient");

	/// Lowest priority first: by the rank of the playback's groups, then fired over held over
	/// ambient, then newer over older.
	public static final Comparator<PlaybackImpl> DRAW_ORDER = Comparator
			.comparingInt((PlaybackImpl playback) -> playback.mixing.rank())
			.thenComparingInt(FeedbackEngine::triggerPriority)
			.thenComparingLong(playback -> playback.sequence);

	/// A held playback is released when its event has not been held for this long. Emit
	/// sites call `hold` once per game tick (50 ms), so this tolerates two missed ticks.
	public static final float HOLD_TIMEOUT_SECONDS = 0.15f;
	/// Frames longer than this advance the clock by only this much, so one long frame
	/// cannot time out every hold or skip most of a short effect.
	public static final float MAX_STEP_SECONDS = 0.05f;

	private final FeedbackServices services;
	private final List<FeedbackRenderer> renderers;

	private final List<PlaybackImpl> playbacks = new ArrayList<>();
	/// The held playbacks of each event, by effect id. An event can hold several effects at
	/// once, one for each rule that matches.
	private final Map<Identifier, Map<Identifier, PlaybackImpl>> holds = new HashMap<>();

	private final MixedFrame scratch = new MixedFrame();
	private final MixedFrame mixed = new MixedFrame();

	private boolean silent;
	private long nextSequence;

	public FeedbackEngine(FeedbackServices services, List<? extends FeedbackRenderer> renderers) {
		this.services = services;
		this.renderers = List.copyOf(renderers);
	}

	/// Plays an effect directly, without an event. Used for effects that are built at
	/// runtime, such as vibrations sent by a server.
	///
	/// @param id     identifies what is playing, for gain lookup
	/// @param group  the group to use if no resource pack assigns `id` one
	/// @param pos    if not null, the playback is attenuated by distance from the camera to it
	/// @param range  the falloff range in blocks, ignored without `pos`
	public PlaybackImpl play(
			Identifier id,
			FeedbackGroup group,
			FeedbackEffect<Object> effect,
			@Nullable Supplier<Vec3> pos,
			float range
	) {
		PlaybackImpl playback = new PlaybackImpl(id, id, effect, group, false, this.nextSequence++, List.of(), pos, range);
		this.playbacks.add(playback);
		return playback;
	}

	public <P> void fire(FeedbackEventImpl<P> event, @Nullable P params, @Nullable Vec3 pos) {
		if (pos != null && this.services.falloff(pos, event.falloffRange()) <= 0f) {
			return;
		}

		for (Identifier effectId : this.services.select(event)) {
			FeedbackEffect<Object> effect = this.services.findEffect(effectId, event.id());
			if (effect == null) {
				continue;
			}
			if (effect instanceof DataFeedbackEffect data && data.holdOnly()) {
				CUtil.LOGGER.warn(
						"Feedback effect '{}' never ends on its own, so event '{}' cannot fire it. Add an envelope or a duration.",
						effectId, event.id()
				);
				continue;
			}

			PlaybackImpl playback = this.createPlayback(event, effectId, effect, false);
			this.refresh(event, playback, params, pos);
			this.playbacks.add(playback);
		}
	}

	/// Holds every effect the event selects. An effect that stops being selected is released
	/// while the others carry on, and one that becomes selected starts from its beginning.
	public <P> void hold(FeedbackEventImpl<P> event, @Nullable P params, @Nullable Vec3 pos) {
		List<Identifier> selected = this.services.select(event);
		Map<Identifier, PlaybackImpl> held = this.holds.computeIfAbsent(event.id(), _ -> new HashMap<>());

		held.entrySet().removeIf(entry -> {
			if (selected.contains(entry.getKey())) {
				return false;
			}
			entry.getValue().release();
			return true;
		});

		for (Identifier effectId : selected) {
			PlaybackImpl playback = held.get(effectId);
			if (playback == null) {
				FeedbackEffect<Object> effect = this.services.findEffect(effectId, event.id());
				if (effect == null) {
					continue;
				}
				playback = this.createPlayback(event, effectId, effect, true);
				this.playbacks.add(playback);
				held.put(effectId, playback);
			}
			this.refresh(event, playback, params, pos);
		}
	}

	/// Ends a hold immediately instead of waiting for it to time out.
	public void release(FeedbackEventImpl<?> event) {
		Map<Identifier, PlaybackImpl> held = this.holds.remove(event.id());
		if (held != null) {
			held.values().forEach(PlaybackImpl::release);
		}
	}

	private PlaybackImpl createPlayback(
			FeedbackEventImpl<?> event,
			Identifier effectId,
			FeedbackEffect<Object> effect,
			boolean held
	) {
		return new PlaybackImpl(
				event.id(), effectId, effect, FeedbackGroup.UNGROUPED, held, this.nextSequence++,
				event.paramNames(), null, event.falloffRange()
		);
	}

	private <P> void refresh(FeedbackEventImpl<P> event, PlaybackImpl playback, @Nullable P params, @Nullable Vec3 pos) {
		playback.params = params;
		event.extractParams(params, playback.paramValues());
		playback.pos = pos == null ? null : () -> pos;
		playback.idleSeconds = 0f;
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
			this.holds.values().removeIf(held -> {
				// a playback that ended by itself stays here until its event stops holding it,
				// so that holding does not restart it
				held.values().removeIf(playback -> {
					playback.idleSeconds += step;
					if (playback.idleSeconds > HOLD_TIMEOUT_SECONDS) {
						playback.release();
						return true;
					}
					return false;
				});
				return held.isEmpty();
			});
		}

		for (PlaybackImpl playback : this.playbacks) {
			playback.mixing = this.services.mixing(playback.effectId, playback.fallbackGroup);
		}
		this.playbacks.sort(DRAW_ORDER);

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

	/// 0 for ambient playbacks, 1 for other held ones, 2 for fired ones.
	private static int triggerPriority(PlaybackImpl playback) {
		if (!playback.held) {
			return 2;
		}
		return playback.eventId.equals(AMBIENT_EVENT) ? 0 : 1;
	}

	private void silenceRenderers() {
		for (FeedbackRenderer renderer : this.renderers) {
			renderer.silence();
		}
	}
}
