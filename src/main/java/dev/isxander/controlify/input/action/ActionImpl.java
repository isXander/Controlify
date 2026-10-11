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

	private final ActionGraph actionGraph;

	public ActionImpl(
			ActionSpec spec,
			@Nullable LogicalInput boundInput,
			@Nullable LogicalInput defaultInput,
			ActivatorConfig<?> activator,
			ActionGraph actionGraph
	) {
		this.spec = spec;
		this.defaultInput = defaultInput;
		this.activatorConfig = activator;
		this.state = new ActionState();
		this.actionGraph = actionGraph;
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
		this.actionGraph.onActionBindingChange(this);
	}

	@Override
	public @Nullable LogicalInput defaultInput() {
		return this.defaultInput;
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
