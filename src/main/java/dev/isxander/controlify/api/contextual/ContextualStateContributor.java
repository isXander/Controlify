package dev.isxander.controlify.api.contextual;

public interface ContextualStateContributor<C extends Context> {
	void contribute(C context, ContextualStateSink sink);

	default ContextualStateContributor<C> andThen(ContextualStateContributor<? super C> other) {
		return combine(this, other);
	}

	static <C extends Context> ContextualStateContributor<C> noop() {
		return (context, sink) -> {};
	}

	@SafeVarargs
	static <C extends Context> ContextualStateContributor<C> combine(
			ContextualStateContributor<? super C>... contributors
	) {
		return (context, sink) -> {
			for (var contributor : contributors) {
				contributor.contribute(context, sink);
			}
		};
	}
}
