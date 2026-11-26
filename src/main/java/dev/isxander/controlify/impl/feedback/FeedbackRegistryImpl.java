/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.api.feedback.FeedbackEvent;
import dev.isxander.controlify.api.feedback.FeedbackRegistry;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.event.EventBus;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/// Holds events and effects. A resource-pack data effect wins over a code effect with the
/// same id. A code effect only plays for the event it was registered for, which is what
/// keeps its params typed.
public final class FeedbackRegistryImpl implements FeedbackRegistry {
	/// The effect id a feedback rule uses to stop the rules below it, so they play nothing.
	public static final Identifier NONE = CUtil.rl("none");

	/// Loading this holder does not construct the registry.
	public static final class Events {
		public static final EventBus<FeedbackRegistry> REGISTER = new EventBus<>();

		private Events() {
		}
	}

	public static final FeedbackRegistryImpl INSTANCE = new FeedbackRegistryImpl();

	private final Map<Identifier, FeedbackEvent<?>> events = new LinkedHashMap<>();
	private final Map<Identifier, CodeEffect> codeEffects = new HashMap<>();
	private volatile Map<Identifier, ? extends FeedbackEffect<Object>> dataEffects = Map.of();
	private boolean locked;

	public FeedbackRegistryImpl() {
	}

	/// Registers Controlify's own events, then lets addons register theirs.
	/// Must be called exactly once during early setup.
	public void initialise() {
		BuiltinFeedback.register(this);
		try {
			Events.REGISTER.emit(this);
		} finally {
			this.locked = true;
		}
	}

	@Override
	public synchronized void registerEvent(@NonNull FeedbackEvent<?> event) {
		this.requireUnlocked();
		Objects.requireNonNull(event, "event");
		if (this.events.putIfAbsent(event.id(), event) != null) {
			throw new IllegalStateException("Feedback event already registered: " + event.id());
		}
	}

	@Override
	public synchronized <P> void registerEffect(@NonNull Identifier effectId, @NonNull FeedbackEvent<P> event, @NonNull FeedbackEffect<? super P> effect) {
		this.requireUnlocked();
		if (this.codeEffects.putIfAbsent(effectId, codeEffect(effectId, event, effect)) != null) {
			throw new IllegalStateException("Feedback effect already registered: " + effectId);
		}
	}

	@Override
	public synchronized <P> void replaceEffect(@NonNull Identifier effectId, @NonNull FeedbackEvent<P> event, @NonNull FeedbackEffect<? super P> effect) {
		this.requireUnlocked();
		this.codeEffects.put(effectId, codeEffect(effectId, event, effect));
	}

	/// Replaces all resource-pack data effects. Called when resources reload.
	public void setDataEffects(Map<Identifier, ? extends FeedbackEffect<Object>> effects) {
		this.dataEffects = Map.copyOf(effects);
	}

	/// @return the effect with this id that can play for `eventId`, or null
	public synchronized @Nullable FeedbackEffect<Object> find(Identifier effectId, Identifier eventId) {
		FeedbackEffect<Object> data = this.dataEffects.get(effectId);
		if (data != null) {
			return data;
		}

		CodeEffect code = this.codeEffects.get(effectId);
		if (code == null) {
			return null;
		}
		if (!code.eventId().equals(eventId)) {
			CUtil.LOGGER.warn(
					"Feedback effect '{}' is registered for event '{}' and cannot play for event '{}'",
					effectId, code.eventId(), eventId
			);
			return null;
		}
		return code.effect();
	}

	/// @return the data effect with this id, or null
	public @Nullable FeedbackEffect<Object> findData(Identifier effectId) {
		return this.dataEffects.get(effectId);
	}

	public synchronized Map<Identifier, FeedbackEvent<?>> events() {
		return Map.copyOf(this.events);
	}

	private void requireUnlocked() {
		if (this.locked) {
			throw new IllegalStateException("Feedback events and effects must be registered during FeedbackEvents.REGISTER");
		}
	}

	@SuppressWarnings("unchecked")
	private static <P> CodeEffect codeEffect(Identifier effectId, FeedbackEvent<P> event, FeedbackEffect<? super P> effect) {
		Objects.requireNonNull(effectId, "effectId");
		Objects.requireNonNull(event, "event");
		Objects.requireNonNull(effect, "effect");
		if (NONE.equals(effectId)) {
			throw new IllegalArgumentException("'" + NONE + "' is reserved for feedback rules that play nothing");
		}
		// safe: the effect is only ever handed playbacks of `event`, whose params are P
		return new CodeEffect(event.id(), (FeedbackEffect<Object>) effect);
	}

	private record CodeEffect(Identifier eventId, FeedbackEffect<Object> effect) {
	}
}
