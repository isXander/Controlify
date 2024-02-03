package dev.isxander.controlify.controller.composable;

import dev.isxander.controlify.controller.composable.gyro.GyroStateC;
import dev.isxander.controlify.controller.composable.impl.ComposableControllerStateImpl;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface ComposableControllerState {
    ComposableControllerState EMPTY = new ComposableControllerStateImpl();

    boolean isButtonDown(ResourceLocation button);

    Set<ResourceLocation> getButtons();

    float getAxisState(ResourceLocation axis);
    Set<ResourceLocation> getAxes();

    float getAxisResting(ResourceLocation axis);

    HatState getHatState(ResourceLocation hat);
    Set<ResourceLocation> getHats();

    GyroStateC getGyroState();

    TouchpadState getTouchpadState();

    default void close() {}
}
