package dev.isxander.controlify.contextual;

public interface Rule<K> {
	/// Used to identify require multiple rules are overriding each other.
	///
	/// For example, with guides this would be the binding and location.
	///
	/// Allows deduplication and short-circuiting done by the rule engine.
	K key();

	ContextualPredicate predicate();

	default boolean matches(ContextualState state) {
		return this.predicate().matches(state);
	}
}
