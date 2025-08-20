package dev.isxander.controlify.input.action;

import dev.isxander.controlify.input.action.gesture.Gesture;
import dev.isxander.controlify.input.action.gesture.NoopGesture;
import net.minecraft.network.chat.Component;

public class ActionImpl implements Action {
    private final ActionSpec spec;
    private final ActionState state;
    private Gesture gesture;
    private final Gesture defaultGesture;

    public ActionImpl(ActionSpec spec, Gesture gesture, Gesture defaultGesture) {
        if (!gesture.supports(spec.channelKind())) {
            throw new IllegalArgumentException("Gesture " + gesture.describe() + " does not support channel kind " + spec.channelKind());
        }

        this.spec = spec;
        this.state = new ActionState();
        this.gesture = gesture;
        this.defaultGesture = defaultGesture;
    }

    @Override
    public ActionSpec spec() {
        return this.spec;
    }

    @Override
    public Channel.Pulse pulseChannel() {
        return this.state;
    }

    @Override
    public Channel.Latch latchChannel() {
        return this.state;
    }

    @Override
    public Channel.Continuous continuousChannel() {
        return this.state;
    }

    public ActionState state() {
        return this.state;
    }

    public Gesture gesture() {
        return this.gesture;
    }

    public void setGesture(Gesture gesture) {
        this.gesture = gesture;
    }

    public Gesture defaultGesture() {
        return this.defaultGesture;
    }

    @Override
    public boolean isUnbound() {
        return this.gesture instanceof NoopGesture;
    }

    @Override
    public Component gestureGlyph() {
        return null;
    }
}
