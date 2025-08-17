package dev.isxander.controlify.input.action;

public sealed interface Channel {
    record Continuous() implements Channel {}
    record Pulse() implements Channel {}
    record Latch() implements Channel {}
}
