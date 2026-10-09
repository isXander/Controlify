package dev.isxander.controlify.utils.event;

@FunctionalInterface
public interface EventSink<T> {
	void accept(T event);
}
