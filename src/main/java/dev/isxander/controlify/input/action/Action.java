package dev.isxander.controlify.input.action;

import net.minecraft.network.chat.Component;

public interface Action {

    ActionSpec spec();

    Channel.Latch latchChannel();
    Channel.Pulse pulseChannel();
    Channel.Continuous continuousChannel();

    boolean isUnbound();

    Component gestureGlyph();

}
