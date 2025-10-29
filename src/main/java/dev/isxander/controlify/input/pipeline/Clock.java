package dev.isxander.controlify.input.pipeline;

public interface Clock extends EventSource<Clock.Tick> {
    Clock STOPPED = event -> () -> {};

    static Clock of(EventSource<Clock.Tick> eventSource) {
        return eventSource::subscribe;
    }

    record Tick(long timestamp, long deltaTimeMs) {}
}
