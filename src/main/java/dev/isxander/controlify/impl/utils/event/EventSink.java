package dev.isxander.controlify.impl.utils.event;

@FunctionalInterface
public interface EventSink<T> {
	void accept(T event);
}
