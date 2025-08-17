package dev.isxander.controlify.input.action;

public interface ActionSnapshot {
    boolean pulse(ActionId<Channel.Pulse> id);

    boolean latch(ActionId<Channel.Latch> id);

    float continuous(ActionId<Channel.Continuous> id);

    long timeNanos();
}
