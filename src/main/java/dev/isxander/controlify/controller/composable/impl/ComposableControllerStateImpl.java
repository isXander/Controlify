package dev.isxander.controlify.controller.composable.impl;

import dev.isxander.controlify.controller.composable.ModifiableControllerState;
import dev.isxander.controlify.controller.composable.TouchpadState;
import dev.isxander.controlify.controller.composable.gyro.GyroStateC;
import dev.isxander.controlify.controller.composable.HatState;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

public class ComposableControllerStateImpl implements ModifiableControllerState {
    private final Map<ResourceLocation, Boolean> buttons;
    private final Map<ResourceLocation, Float> axes;
    private final Map<ResourceLocation, Float> restingAxes;
    private final Map<ResourceLocation, HatState> hats;
    private GyroStateC gyroState = GyroStateC.ZERO;
    private TouchpadState touchpadState = TouchpadState.empty(0);

    public ComposableControllerStateImpl() {
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
    public GyroStateC getGyroState() {
        return gyroState;
    }

    @Override
    public TouchpadState getTouchpadState() {
        return touchpadState;
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

    public void setGyroState(GyroStateC value) {
        gyroState = value;
    }

    public void setTouchpadState(TouchpadState value) {
        touchpadState = value;
    }
}
