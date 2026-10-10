package dev.isxander.controlify.input.action;

import dev.isxander.controlify.input.action.activator.ActivatorConfig;
import dev.isxander.controlify.input.action.activator.NoopActivator;
import dev.isxander.controlify.input.logical.LogicalInput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ActionImpl implements Action, Comparable<ActionImpl> {
	private final ActionSpec spec;
	private final ActionState state;
	private @Nullable LogicalInput boundInput;
	private final @Nullable LogicalInput defaultInput;
	private Activator activator;
	private final ActivatorConfig<?> activatorConfig;

	public ActionImpl(
			ActionSpec spec,
			@Nullable LogicalInput boundInput,
			@Nullable LogicalInput defaultInput,
			ActivatorConfig<?> activator
	) {
		this.spec = spec;
		this.defaultInput = defaultInput;
		this.activatorConfig = activator;
		this.setBoundInput(boundInput);
		this.state = new ActionState();
	}

	@Override
	public @Nullable LogicalInput boundInput() {
		return this.boundInput;
	}

	@Override
	public void setBoundInput(@Nullable LogicalInput boundInput) {
		this.boundInput = boundInput;
		this.activator = boundInput != null
				? this.activatorConfig.create(boundInput)
				: new NoopActivator();
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
