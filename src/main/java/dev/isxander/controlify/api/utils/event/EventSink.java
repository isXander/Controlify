package dev.isxander.controlify.api.utils.event;

@FunctionalInterface
public interface EventSink<T> {
	void accept(T event);
}
