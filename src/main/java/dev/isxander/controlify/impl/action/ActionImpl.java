/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.action.ActionSpec;
import dev.isxander.controlify.api.action.Gesture;
import dev.isxander.controlify.api.action.Channel;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.action.activator.Activator;
import dev.isxander.controlify.impl.action.activator.ActivatorConfig;
import dev.isxander.controlify.impl.action.activator.LeafConfig;
import dev.isxander.controlify.impl.action.activator.NoopActivator;
import dev.isxander.controlify.impl.utils.CUtil;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionImpl implements Action, Comparable<ActionImpl> {
	private final ActionSpec spec;
	private final ActionState state;
	private List<ActionBinding> bindings;
	private List<ActionBinding> defaultBindings;
	private Set<LogicalInput> boundInputs;
	private Activator activator;
	private final ActivatorConfig<?> activatorConfig;

	private final ActionDispatcher actionDispatcher;

	public ActionImpl(
			ActionSpec spec,
			List<ActionBinding> bindings,
			List<ActionBinding> defaultBindings,
			ActivatorConfig<?> activator,
			ActionDispatcher actionDispatcher
	) {
		this.spec = spec;
		this.defaultBindings = List.copyOf(defaultBindings);
		this.activatorConfig = activator;
		this.state = new ActionState();
		this.actionDispatcher = actionDispatcher;
		this.setBindingsSilently(bindings);
	}

	@Override
	public @NonNull ActionSpec spec() {
		return this.spec;
	}

	@Override
	public @NonNull List<ActionBinding> bindings() {
		return this.bindings;
	}

	/// The distinct inputs across every binding.
	public Set<LogicalInput> boundInputs() {
		return this.boundInputs;
	}

	@Override
	public void setBindings(@NonNull List<ActionBinding> bindings) {
		this.setBindingsSilently(bindings);
		this.reset();
		this.actionDispatcher.onActionBindingChange(this);
	}

	private void setBindingsSilently(List<ActionBinding> bindings) {
		this.warnUnappliedGestures(bindings);
		this.bindings = List.copyOf(bindings);
		this.boundInputs = this.bindings.stream()
				.map(ActionBinding::input)
				.collect(Collectors.toUnmodifiableSet());
	}

	/// A binding's gesture is ignored unless the activator has an editable leaf of the same kind.
	private void warnUnappliedGestures(List<ActionBinding> bindings) {
		Optional<LeafConfig> editableLeaf = this.activatorConfig.editableLeaf();
		for (ActionBinding binding : bindings) {
			if (binding.gesture().isEmpty()) {
				continue;
			}
			Gesture gesture = binding.gesture().get();
			if (editableLeaf.isEmpty()) {
				CUtil.LOGGER.warn("Ignoring gesture '{}' on {} for action {}: its activator has no editable gesture",
						gesture.getSerializedName(), binding.input(), this.spec.id());
			} else if (editableLeaf.get().gestureFor(binding).orElse(null) != gesture) {
				CUtil.LOGGER.warn("Ignoring gesture '{}' on {} for action {}: its editable activator only accepts {} gestures",
						gesture.getSerializedName(), binding.input(), this.spec.id(),
						editableLeaf.get().gesture().map(leafGesture -> leafGesture.kind().name().toLowerCase(Locale.ROOT)).orElse("no"));
			}
		}
	}

	@Override
	public @NonNull List<ActionBinding> defaultBindings() {
		return this.defaultBindings;
	}

	/// Refreshes resource-defined defaults without changing the current bindings or activator state.
	public void setDefaultBindings(List<ActionBinding> defaultBindings) {
		this.defaultBindings = List.copyOf(defaultBindings);
	}

	/// Whether another action currently receives this input too, given the active layers.
	public boolean isInputShared(LogicalInput input) {
		return this.actionDispatcher.isSharedWithOtherActions(this, input);
	}

	public ActivatorConfig<?> activatorConfig() {
		return this.activatorConfig;
	}

	@Override
	public Channel.@NonNull Pulse pulse() {
		return this.state();
	}

	@Override
	public Channel.@NonNull Latch latch() {
		return this.state();
	}

	@Override
	public Channel.@NonNull Continuous continuous() {
		return this.state();
	}

	public void reset() {
		this.activator = this.bindings.isEmpty()
				? new NoopActivator()
				: this.activatorConfig.create(this.bindings);
		this.state.reset();
	}

	public ActionState state() {
		return this.state;
	}

	public Activator activator() {
		return this.activator;
	}

	@Override
	public int compareTo(@NonNull ActionImpl o) {
		return this.spec().compareTo(o.spec());
	}
}
