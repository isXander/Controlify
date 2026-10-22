package dev.isxander.controlify.api.action;

/// Receives action definitions during {@link ActionEvents#REGISTER_ACTIONS}.
public interface ActionSpecRegistry {
	/// Throws if registration has closed or the identifier is already registered.
	ActionAccessor register(ActionSpec actionSpec);
}
