package dev.isxander.controlify.controller.impl;

import com.google.common.base.Joiner;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.ModifiableControllerState;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

public class ControllerStateImpl implements ModifiableControllerState {
    private final Map<ResourceLocation, Boolean> buttons;
    private final Map<ResourceLocation, Float> axes;
    private final Map<ResourceLocation, Float> restingAxes;
    private final Map<ResourceLocation, HatState> hats;

    public ControllerStateImpl() {
        this.buttons = new Object2BooleanArrayMap<>();
        this.axes = new Object2FloatArrayMap<>();
        this.restingAxes = new Object2FloatArrayMap<>();
        this.hats = new Object2ObjectArrayMap<>();
    }

    @Override
    public boolean isButtonDown(ResourceLocation button) {
        return buttons.getOrDefault(button, false);
    }

    @Override
    public Set<ResourceLocation> getButtons() {
        return buttons.keySet();
    }

    @Override
    public float getAxisState(ResourceLocation axis) {
        return axes.getOrDefault(axis, 0f);
    }

    @Override
    public Set<ResourceLocation> getAxes() {
        return axes.keySet();
    }

    @Override
    public float getAxisResting(ResourceLocation axis) {
        return restingAxes.getOrDefault(axis, 0f);
    }

    @Override
    public HatState getHatState(ResourceLocation hat) {
        return hats.getOrDefault(hat, HatState.CENTERED);
    }

    @Override
    public Set<ResourceLocation> getHats() {
        return hats.keySet();
    }

    @Override
    public void setButton(ResourceLocation button, boolean value) {
        buttons.put(button, value);
    }

    @Override
    public void setAxis(ResourceLocation axis, float value) {
        axes.put(axis, value);
    }

    public void setRestingAxis(ResourceLocation axis, float value) {
        if (!axes.containsKey(axis))
            throw new IllegalArgumentException("Cannot set resting axis for axis that doesn't exist");

        restingAxes.put(axis, value);
    }

    @Override
    public void setHat(ResourceLocation hat, HatState value) {
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
