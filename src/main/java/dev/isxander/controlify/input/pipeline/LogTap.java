package dev.isxander.controlify.input.pipeline;

import dev.isxander.controlify.utils.log.ControlifyLogger;

/**
 * A tap that logs events to a {@link ControlifyLogger}.
 * This is useful for debugging purposes, allowing you to see the events flowing through the pipeline.
 * @param <T> the type of events this tap handles
 */
public class LogTap<T> implements EventTap<T> {
    private final ControlifyLogger logger;

    public LogTap(ControlifyLogger logger) {
        this.logger = logger;
    }

    @Override
    public void onEvent(T event) {
        this.logger.debugLog("DebugTap: Received event: {}", event);
    }
}
