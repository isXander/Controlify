package dev.isxander.controlify.input.action;

import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.input.action.gesture.Gesture;
import dev.isxander.controlify.input.action.gesture.NoopGesture;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ActionImpl implements Action, Comparable<ActionImpl> {
    private final ActionSpec spec;
    private final ActionState state;
    private Gesture gesture, defaultGesture;

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

    ActionState state() {
        return this.state;
    }

    public Gesture gesture() {
        return this.gesture;
    }

    public void setGesture(Gesture gesture) {
        if (!gesture.supports(this.spec.channelKind())) {
            throw new IllegalArgumentException("Gesture " + gesture.describe() + " does not support channel kind " + this.spec.channelKind());
        }
        this.gesture = gesture;
    }

    public Gesture defaultGesture() {
        return this.defaultGesture;
    }

    public void setDefaultGesture(Gesture defaultGesture) {
        if (!defaultGesture.supports(this.spec.channelKind())) {
            throw new IllegalArgumentException("Gesture " + defaultGesture.describe() + " does not support channel kind " + this.spec.channelKind());
        }
        this.defaultGesture = defaultGesture;
    }

    @Override
    public void resetToDefaultBinding() {
        this.gesture = this.defaultGesture;
    }

    @Override
    public boolean isUnbound() {
        return this.gesture instanceof NoopGesture;
    }

    @Override
    public boolean isDefaultBound() {
        return this.gesture.equals(this.defaultGesture);
    }

    @Override
    public Component gestureGlyph() {
        return BindingFontHelper.binding(this.gesture().monitoredInputs());
    }

    @Override
    public int compareTo(@NotNull ActionImpl o) {
        return this.spec().compareTo(o.spec());
    }
}
