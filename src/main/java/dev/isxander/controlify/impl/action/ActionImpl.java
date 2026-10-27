/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.action.ActionSpec;
import dev.isxander.controlify.api.action.Channel;
import dev.isxander.controlify.impl.action.activator.Activator;
import dev.isxander.controlify.impl.action.activator.ActivatorConfig;
import dev.isxander.controlify.impl.action.activator.NoopActivator;
import dev.isxander.controlify.api.input.LogicalInput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ActionImpl implements Action, Comparable<ActionImpl> {
	private final ActionSpec spec;
	private final ActionState state;
	private @Nullable LogicalInput boundInput;
	private @Nullable LogicalInput defaultInput;
	private Activator activator;
	private final ActivatorConfig<?> activatorConfig;

	private final ActionDispatcher actionDispatcher;

	public ActionImpl(
			ActionSpec spec,
			@Nullable LogicalInput boundInput,
			@Nullable LogicalInput defaultInput,
			ActivatorConfig<?> activator,
			ActionDispatcher actionDispatcher
	) {
		this.spec = spec;
		this.defaultInput = defaultInput;
		this.activatorConfig = activator;
		this.state = new ActionState();
		this.actionDispatcher = actionDispatcher;
		this.boundInput = boundInput;
	}

	@Override
	public ActionSpec spec() {
		return this.spec;
	}

	@Override
	public @Nullable LogicalInput boundInput() {
		return this.boundInput;
	}

	@Override
	public void setBoundInput(@Nullable LogicalInput boundInput) {
		this.boundInput = boundInput;
		this.reset();
		this.actionDispatcher.onActionBindingChange(this);
	}

	@Override
	public @Nullable LogicalInput defaultInput() {
		return this.defaultInput;
	}

	/// Refreshes resource-defined defaults without changing the current binding or activator state.
	public void setDefaultInput(@Nullable LogicalInput defaultInput) {
		this.defaultInput = defaultInput;
	}

	@Override
	public Channel.Pulse pulse() {
		return this.state();
	}

	@Override
	public Channel.Latch latch() {
		return this.state();
	}

	@Override
	public Channel.Continuous continuous() {
		return this.state();
	}

	public void reset() {
		this.activator = this.boundInput() != null
				? this.activatorConfig.create(this.boundInput())
				: new NoopActivator();
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
