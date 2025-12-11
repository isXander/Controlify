package dev.isxander.controlify.controller.impl;

import com.google.common.base.Joiner;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.ModifiableControllerState;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;

public class ControllerStateImpl implements ModifiableControllerState {
    private final Map<Identifier, Boolean> buttons;
    private final Map<Identifier, Float> axes;
    private final Map<Identifier, Float> restingAxes;
    private final Map<Identifier, HatState> hats;

    public ControllerStateImpl() {
        this.buttons = new Object2BooleanArrayMap<>();
        this.axes = new Object2FloatArrayMap<>();
        this.restingAxes = new Object2FloatArrayMap<>();
        this.hats = new Object2ObjectArrayMap<>();
    }

    @Override
    public boolean isButtonDown(Identifier button) {
        return buttons.getOrDefault(button, false);
    }

    @Override
    public Set<Identifier> getButtons() {
        return buttons.keySet();
    }

    @Override
    public float getAxisState(Identifier axis) {
        return axes.getOrDefault(axis, 0f);
    }

    @Override
    public Set<Identifier> getAxes() {
        return axes.keySet();
    }

    @Override
    public float getAxisResting(Identifier axis) {
        return restingAxes.getOrDefault(axis, 0f);
    }

    @Override
    public HatState getHatState(Identifier hat) {
        return hats.getOrDefault(hat, HatState.CENTERED);
    }

    @Override
    public Set<Identifier> getHats() {
        return hats.keySet();
    }

    @Override
    public void setButton(Identifier button, boolean value) {
        buttons.put(button, value);
    }

    @Override
    public void setAxis(Identifier axis, float value) {
        axes.put(axis, value);
    }

    public void setRestingAxis(Identifier axis, float value) {
        if (!axes.containsKey(axis))
            throw new IllegalArgumentException("Cannot set resting axis for axis that doesn't exist");

        restingAxes.put(axis, value);
    }

    @Override
    public void setHat(Identifier hat, HatState value) {
        hats.put(hat, value);
    }

    @Override
    public void clearState() {
        this.buttons.clear();
        this.axes.clear();
        this.restingAxes.clear();
        this.hats.clear();
    }

    public String toDebugString() {
        Joiner.MapJoiner joiner = Joiner.on(",").withKeyValueSeparator("=");
        return "ControllerState{axes:%s,buttons:%s,hats:%s}".formatted(
                joiner.join(axes),
                joiner.join(buttons),
                joiner.join(hats)
        );
    }
}
