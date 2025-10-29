package dev.isxander.controlify.driver.sdl;

import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.input.input.SensorType;
import dev.isxander.sdl3java.api.gamepad.SDL_GamepadAxis;
import dev.isxander.sdl3java.api.gamepad.SDL_GamepadButton;
import dev.isxander.sdl3java.api.sensor.SDL_SensorType;
import net.minecraft.resources.ResourceLocation;
import org.intellij.lang.annotations.MagicConstant;

import java.util.stream.IntStream;

import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadAxis.*;
import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadButton.*;
import static dev.isxander.sdl3java.api.sensor.SDL_SensorType.*;

public final class SdlInputConversions {
    // fast array lookup
    private static final ResourceLocation[] GAMEPAD_BUTTONS_MAP = IntStream.range(0, SDL_GamepadButton.SDL_GAMEPAD_BUTTON_COUNT)
            .mapToObj(i -> switch (i) {
                case SDL_GAMEPAD_BUTTON_SOUTH -> GamepadInputs.SOUTH_BUTTON;
                case SDL_GAMEPAD_BUTTON_EAST -> GamepadInputs.EAST_BUTTON;
                case SDL_GAMEPAD_BUTTON_WEST -> GamepadInputs.WEST_BUTTON;
                case SDL_GAMEPAD_BUTTON_NORTH -> GamepadInputs.NORTH_BUTTON;
                case SDL_GAMEPAD_BUTTON_BACK -> GamepadInputs.BACK_BUTTON;
                case SDL_GAMEPAD_BUTTON_GUIDE -> GamepadInputs.GUIDE_BUTTON;
                case SDL_GAMEPAD_BUTTON_START -> GamepadInputs.START_BUTTON;
                case SDL_GAMEPAD_BUTTON_LEFT_STICK -> GamepadInputs.LEFT_STICK_BUTTON;
                case SDL_GAMEPAD_BUTTON_RIGHT_STICK -> GamepadInputs.RIGHT_STICK_BUTTON;
                case SDL_GAMEPAD_BUTTON_LEFT_SHOULDER -> GamepadInputs.LEFT_SHOULDER_BUTTON;
                case SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER -> GamepadInputs.RIGHT_SHOULDER_BUTTON;
                case SDL_GAMEPAD_BUTTON_DPAD_UP -> GamepadInputs.DPAD_UP_BUTTON;
                case SDL_GAMEPAD_BUTTON_DPAD_DOWN -> GamepadInputs.DPAD_DOWN_BUTTON;
                case SDL_GAMEPAD_BUTTON_DPAD_LEFT -> GamepadInputs.DPAD_LEFT_BUTTON;
                case SDL_GAMEPAD_BUTTON_DPAD_RIGHT -> GamepadInputs.DPAD_RIGHT_BUTTON;
                case SDL_GAMEPAD_BUTTON_MISC1 -> GamepadInputs.MISC_1_BUTTON;
                case SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1 -> GamepadInputs.RIGHT_PADDLE_1_BUTTON;
                case SDL_GAMEPAD_BUTTON_LEFT_PADDLE1 -> GamepadInputs.LEFT_PADDLE_1_BUTTON;
                case SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2 -> GamepadInputs.RIGHT_PADDLE_2_BUTTON;
                case SDL_GAMEPAD_BUTTON_LEFT_PADDLE2 -> GamepadInputs.LEFT_PADDLE_2_BUTTON;
                case SDL_GAMEPAD_BUTTON_TOUCHPAD -> GamepadInputs.TOUCHPAD_1_BUTTON;
                case SDL_GAMEPAD_BUTTON_MISC2 -> GamepadInputs.MISC_2_BUTTON;
                case SDL_GAMEPAD_BUTTON_MISC3 -> GamepadInputs.MISC_3_BUTTON;
                case SDL_GAMEPAD_BUTTON_MISC4 -> GamepadInputs.MISC_4_BUTTON;
                case SDL_GAMEPAD_BUTTON_MISC5 -> GamepadInputs.MISC_5_BUTTON;
                case SDL_GAMEPAD_BUTTON_MISC6 -> GamepadInputs.MISC_6_BUTTON;
                default -> throw new IllegalArgumentException("Unknown SDL Gamepad Button: " + i);
            })
            .toArray(ResourceLocation[]::new);

    @SuppressWarnings("PointlessBitwiseExpression")
    private static final ResourceLocation[] GAMEPAD_AXES_MAP = IntStream.range(0, SDL_GAMEPAD_AXIS_COUNT << 1)
            .mapToObj(i -> switch (i) {
                case (SDL_GAMEPAD_AXIS_LEFTX << 1) | 0 -> GamepadInputs.LEFT_STICK_AXIS_RIGHT;
                case (SDL_GAMEPAD_AXIS_LEFTX << 1) | 1 -> GamepadInputs.LEFT_STICK_AXIS_LEFT;
                case (SDL_GAMEPAD_AXIS_LEFTY << 1) | 0 -> GamepadInputs.LEFT_STICK_AXIS_DOWN;
                case (SDL_GAMEPAD_AXIS_LEFTY << 1) | 1 -> GamepadInputs.LEFT_STICK_AXIS_UP;
                case (SDL_GAMEPAD_AXIS_RIGHTX << 1) | 0 -> GamepadInputs.RIGHT_STICK_AXIS_RIGHT;
                case (SDL_GAMEPAD_AXIS_RIGHTX << 1) | 1 -> GamepadInputs.RIGHT_STICK_AXIS_LEFT;
                case (SDL_GAMEPAD_AXIS_RIGHTY << 1) | 0 -> GamepadInputs.RIGHT_STICK_AXIS_DOWN;
                case (SDL_GAMEPAD_AXIS_RIGHTY << 1) | 1 -> GamepadInputs.RIGHT_STICK_AXIS_UP;
                case (SDL_GAMEPAD_AXIS_LEFT_TRIGGER << 1) | 0,
                     (SDL_GAMEPAD_AXIS_LEFT_TRIGGER << 1) | 1 -> GamepadInputs.LEFT_TRIGGER_AXIS;
                case (SDL_GAMEPAD_AXIS_RIGHT_TRIGGER << 1) | 0,
                     (SDL_GAMEPAD_AXIS_RIGHT_TRIGGER << 1) | 1 -> GamepadInputs.RIGHT_TRIGGER_AXIS;
                default -> throw new IllegalArgumentException("Unknown SDL Gamepad Axis: " + i);
            })
            .toArray(ResourceLocation[]::new);

    private static final SensorType[] GAMEPAD_SENSOR_MAP = IntStream.range(0, SDL_SENSOR_COUNT)
            .mapToObj(i -> switch (i) {
                case SDL_SENSOR_GYRO -> SensorType.GYROSCOPE;
                case SDL_SENSOR_GYRO_L ->  SensorType.GYROSCOPE_L;
                case SDL_SENSOR_GYRO_R ->  SensorType.GYROSCOPE_R;
                case SDL_SENSOR_ACCEL -> SensorType.ACCELEROMETER;
                case SDL_SENSOR_ACCEL_L -> SensorType.ACCELEROMETER_L;
                case SDL_SENSOR_ACCEL_R -> SensorType.ACCELEROMETER_R;
                default -> null;
            })
            .toArray(SensorType[]::new);

    public static ResourceLocation mapGamepadButton(
            @MagicConstant(valuesFromClass = SDL_GamepadButton.class) int button
    ) {
        return GAMEPAD_BUTTONS_MAP[button];
    }

    public static ResourceLocation mapGamepadAxis(
            @MagicConstant(valuesFromClass = SDL_GamepadAxis.class) int axis, boolean positive
    ) {
        return GAMEPAD_AXES_MAP[(axis << 1) | (positive ? 0 : 1)];
    }

    public static SensorType mapSensorType(
            @MagicConstant(valuesFromClass = SDL_SensorType.class) int sensorType
    ) {
        if (sensorType < 0 || sensorType >= GAMEPAD_SENSOR_MAP.length) {
            return null;
        }
        return GAMEPAD_SENSOR_MAP[sensorType];
    }
}
