package dev.isxander.controlify.contextual.api;

public interface ContextualStateContributor<C extends Context> {
	/// Ran each update tick
	/// The new alternative to the Guide API's old `Fact<C>` where it was a Predicate per fact!
	void contribute(C context, ContextualStateSink sink);
}
