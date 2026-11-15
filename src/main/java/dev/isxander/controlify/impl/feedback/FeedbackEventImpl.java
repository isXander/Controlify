/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.contextual.ContextualStateSink;
import dev.isxander.controlify.api.feedback.FeedbackEvent;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/// An event definition. Holds no playback state; playbacks live in the engine of the controller.
public final class FeedbackEventImpl<P> implements FeedbackEvent<P> {
	public static final float DEFAULT_FALLOFF_RANGE = 32f;

	private final Identifier id;
	private final List<NamedParam<P>> params;
	private final List<String> paramNames;
	private final boolean selection;
	private final float falloffRange;

	private FeedbackEventImpl(Identifier id, List<NamedParam<P>> params, boolean selection, float falloffRange) {
		this.id = id;
		this.params = List.copyOf(params);
		this.paramNames = this.params.stream().map(NamedParam::name).toList();
		this.selection = selection;
		this.falloffRange = falloffRange;
	}

	public static FeedbackEvent.Builder builder(Identifier id) {
		return new UntypedBuilder(Objects.requireNonNull(id, "id"));
	}

	@Override
	public Identifier id() {
		return this.id;
	}

	/// Whether this event consults selection rules before falling back to the effect with its own id.
	public boolean usesSelection() {
		return this.selection;
	}

	public float falloffRange() {
		return this.falloffRange;
	}

	/// The names of the floats this event exposes to data-driven effects, in declaration order.
	List<String> paramNames() {
		return this.paramNames;
	}

	/// Writes the named floats extracted from `value` into `into`, in [#paramNames] order.
	void extractParams(@Nullable P value, float[] into) {
		for (int i = 0; i < this.params.size(); i++) {
			into[i] = value == null ? 0f : this.params.get(i).source().get(value);
		}
	}

	/// Fires on a specific controller rather than the current one.
	public static <P> void fireOn(FeedbackEvent<P> event, ControllerImpl controller, @Nullable P params) {
		controller.feedback().fire((FeedbackEventImpl<P>) event, params, null, null);
	}

	@Override
	public void fire(@Nullable P params, @Nullable Consumer<ContextualStateSink> subject) {
		currentEngine().ifPresent(engine -> engine.fire(this, params, null, subject));
	}

	@Override
	public void fireAt(@NonNull Vec3 pos, @Nullable P params, @Nullable Consumer<ContextualStateSink> subject) {
		currentEngine().ifPresent(engine -> engine.fire(this, params, pos, subject));
	}

	@Override
	public void hold(@Nullable P params, @Nullable Consumer<ContextualStateSink> subject) {
		currentEngine().ifPresent(engine -> engine.hold(this, params, null, subject));
	}

	@Override
	public void holdAt(@NonNull Vec3 pos, @Nullable P params, @Nullable Consumer<ContextualStateSink> subject) {
		currentEngine().ifPresent(engine -> engine.hold(this, params, pos, subject));
	}

	@Override
	public void release() {
		currentEngine().ifPresent(engine -> engine.release(this));
	}

	private static Optional<FeedbackEngine> currentEngine() {
		return Controlify.instance().getCurrentController().map(ControllerImpl::feedback);
	}

	@Override
	public String toString() {
		return "FeedbackEvent[" + this.id + "]";
	}

	private record NamedParam<P>(String name, ParamSource<P> source) {
	}

	private static final class UntypedBuilder implements FeedbackEvent.Builder {
		private final Identifier id;
		private boolean selection;
		private float falloffRange = DEFAULT_FALLOFF_RANGE;

		private UntypedBuilder(Identifier id) {
			this.id = id;
		}

		@Override
		public <P> FeedbackEvent.TypedBuilder<P> param(@NonNull String name, @NonNull ParamSource<P> source) {
			return new TypedBuilderImpl<P>(this.id, this.selection, this.falloffRange).param(name, source);
		}

		@Override
		public FeedbackEvent.Builder selection() {
			this.selection = true;
			return this;
		}

		@Override
		public FeedbackEvent.Builder falloffRange(float blocks) {
			this.falloffRange = checkRange(blocks);
			return this;
		}

		@Override
		public FeedbackEvent<Void> build() {
			return new FeedbackEventImpl<>(this.id, List.of(), this.selection, this.falloffRange);
		}
	}

	private static final class TypedBuilderImpl<P> implements FeedbackEvent.TypedBuilder<P> {
		private final Identifier id;
		private final List<NamedParam<P>> params = new ArrayList<>();
		private boolean selection;
		private float falloffRange;

		private TypedBuilderImpl(Identifier id, boolean selection, float falloffRange) {
			this.id = id;
			this.selection = selection;
			this.falloffRange = falloffRange;
		}

		@Override
		public FeedbackEvent.TypedBuilder<P> param(@NonNull String name, @NonNull ParamSource<P> source) {
			Objects.requireNonNull(name, "name");
			Objects.requireNonNull(source, "source");
			if (this.params.stream().anyMatch(param -> param.name().equals(name))) {
				throw new IllegalArgumentException("Duplicate feedback param '" + name + "' on event " + this.id);
			}
			this.params.add(new NamedParam<>(name, source));
			return this;
		}

		@Override
		public FeedbackEvent.TypedBuilder<P> selection() {
			this.selection = true;
			return this;
		}

		@Override
		public FeedbackEvent.TypedBuilder<P> falloffRange(float blocks) {
			this.falloffRange = checkRange(blocks);
			return this;
		}

		@Override
		public FeedbackEvent<P> build() {
			return new FeedbackEventImpl<>(this.id, this.params, this.selection, this.falloffRange);
		}
	}

	private static float checkRange(float blocks) {
		if (!Float.isFinite(blocks) || blocks <= 0f) {
			throw new IllegalArgumentException("Falloff range must be finite and positive");
		}
		return blocks;
	}
}
