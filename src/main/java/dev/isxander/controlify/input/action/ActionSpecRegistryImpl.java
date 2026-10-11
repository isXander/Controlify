package dev.isxander.controlify.input.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionSpecRegistryImpl implements ActionSpecRegistry {
	public static final ActionSpecRegistryImpl INSTANCE = new ActionSpecRegistryImpl();

	private final List<ActionSpec> registeredSpecs = new ArrayList<>();

	private boolean locked = false;

	@Override
	public ActionAccessor register(ActionSpec actionSpec) {
		this.checkLocked(false);
		this.registeredSpecs.add(actionSpec);

		return new ActionAccessorImpl(actionSpec.id());
	}

	public List<ActionSpec> getActionSpecs() {
		this.checkLocked(true);
		return Collections.unmodifiableList(this.registeredSpecs);
	}

	public void lock() {
		this.locked = true;
	}

	private void checkLocked(boolean required) {
		if (required) {
			if (!this.locked) {
				throw new IllegalStateException("Registry must be locked to perform this operation.");
			}
		} else {
			if (this.locked) {
				throw new IllegalStateException("Registry must be unlocked to perform this operation.");
			}
		}
	}
}
