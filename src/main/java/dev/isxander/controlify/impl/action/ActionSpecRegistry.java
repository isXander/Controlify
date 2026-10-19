package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionSpec;

public interface ActionSpecRegistry {
	ActionAccessor register(ActionSpec actionSpec);
}
