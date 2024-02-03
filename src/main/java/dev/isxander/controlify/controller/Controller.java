package dev.isxander.controlify.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.composable.ComposableControllerState;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.rumble.RumbleCapable;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public interface Controller<C extends ControllerConfig> {
    String uid();
    UniqueControllerID joystickId();

    ControllerBindings bindings();

    ComposableControllerState state();
    ComposableControllerState prevState();

    C config();
    C defaultConfig();
    void resetConfig();
    void setConfig(Gson gson, JsonElement json);

    ControllerType type();

    String name();

    void updateState();
    void clearState();

    default void close() {}

    RumbleManager rumbleManager();
    boolean supportsRumble();

    default boolean supportsGyro() {
        return false;
    }

    default BatteryLevel batteryLevel() {
        return BatteryLevel.UNKNOWN;
    }

    default boolean canBeUsed() {
        return true;
    }

    int axisCount();

    int buttonCount();

    int hatCount();

    String kind();
}
