package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.touchpad.Touchpads;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl3java.api.gamepad.SDL_Gamepad;
import dev.isxander.sdl3java.api.joystick.SDL_Joystick;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickGUID;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.api.properties.SDL_PropertiesID;
import dev.isxander.sdl3java.api.sensor.SDL_SensorType;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static dev.isxander.controlify.utils.CUtil.*;
import static dev.isxander.sdl3java.api.error.SdlError.*;
import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadAxis.*;
import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadButton.*;
import static dev.isxander.sdl3java.api.gamepad.SdlGamepad.*;
import static dev.isxander.sdl3java.api.sensor.SDL_SensorType.*;

public class SDL3GamepadDriver extends SDLCommonDriver<SDL_Gamepad> {
    
    private InputComponent inputComponent;
    private GyroComponent gyroComponent;
    private TouchpadComponent touchpadComponent;

    private final boolean isGryoSupported;

    private final int numTouchpads;
    
    public SDL3GamepadDriver(SDL_Gamepad ptrController, SDL_JoystickID jid, ControllerType type, ControlifyLogger logger) {
        super(ptrController, jid, type, logger);
 
        this.isGryoSupported = SDL_GamepadHasSensor(ptrController, SDL_SensorType.SDL_SENSOR_GYRO);
        this.numTouchpads = SDL_GetNumGamepadTouchpads(ptrController);

        if (this.isGryoSupported) {
            SDL_SetGamepadSensorEnabled(ptrController, SDL_SensorType.SDL_SENSOR_GYRO, true);
        }

    }

    @Override
    public void addComponents(ControllerEntity controller) {
        super.addComponents(controller);

        controller.setComponent(
                this.inputComponent = new InputComponent(
                        controller,
                        21,
                        10,
                        0,
                        true,
                        GamepadInputs.DEADZONE_GROUPS,
                        controller.info().type().mappingId()
                )
        );
        
        if (this.isGryoSupported) {
            controller.setComponent(this.gyroComponent = new GyroComponent());
        }

        if (this.numTouchpads > 0) {
            controller.setComponent(this.touchpadComponent = new TouchpadComponent(
                    new Touchpads(
                            IntStream.range(0, numTouchpads)
                                .mapToObj(i ->
                                        new Touchpads.Touchpad(
                                                SDL_GetNumGamepadTouchpadFingers(ptrController, i)
                                        )
                                ).toArray(Touchpads.Touchpad[]::new)
                    )
            ));
        }
    }
    
    @Override
    public void update(ControllerEntity controller, boolean outOfFocus) {
        super.update(controller, outOfFocus);
        
        this.updateInput();
        this.updateGyro();
        this.updateTouchpad();
    }

    private void updateInput() {
        ControllerStateImpl state = new ControllerStateImpl();
        // Axis values are in the range [-32768, 32767] (short)
        // https://wiki.libsdl.org/SDL3/SDL_GameControllerGetAxis
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_RIGHT, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrController, SDL_GAMEPAD_AXIS_LEFTX))));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_LEFT, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrController, SDL_GAMEPAD_AXIS_LEFTX))));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_UP, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrController, SDL_GAMEPAD_AXIS_LEFTY))));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_DOWN, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrController, SDL_GAMEPAD_AXIS_LEFTY))));

        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrController, SDL_GAMEPAD_AXIS_RIGHTX))));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_LEFT, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrController, SDL_GAMEPAD_AXIS_RIGHTX))));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_UP, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrController, SDL_GAMEPAD_AXIS_RIGHTY))));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_DOWN, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrController, SDL_GAMEPAD_AXIS_RIGHTY))));

        // Triggers are in the range [0, 32767] (thanks SDL!)
        state.setAxis(GamepadInputs.LEFT_TRIGGER_AXIS, mapShortToFloat(SDL_GetGamepadAxis(ptrController, SDL_GAMEPAD_AXIS_LEFT_TRIGGER)));
        state.setAxis(GamepadInputs.RIGHT_TRIGGER_AXIS, mapShortToFloat(SDL_GetGamepadAxis(ptrController, SDL_GAMEPAD_AXIS_RIGHT_TRIGGER)));

        state.setButton(GamepadInputs.SOUTH_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_SOUTH));
        state.setButton(GamepadInputs.EAST_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_EAST));
        state.setButton(GamepadInputs.WEST_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_WEST));
        state.setButton(GamepadInputs.NORTH_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_NORTH));

        state.setButton(GamepadInputs.LEFT_SHOULDER_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_LEFT_SHOULDER));
        state.setButton(GamepadInputs.RIGHT_SHOULDER_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER));

        state.setButton(GamepadInputs.BACK_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_BACK));
        state.setButton(GamepadInputs.START_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_START));
        state.setButton(GamepadInputs.GUIDE_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_GUIDE));

        state.setButton(GamepadInputs.DPAD_UP_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_DPAD_UP));
        state.setButton(GamepadInputs.DPAD_DOWN_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_DPAD_DOWN));
        state.setButton(GamepadInputs.DPAD_LEFT_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_DPAD_LEFT));
        state.setButton(GamepadInputs.DPAD_RIGHT_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_DPAD_RIGHT));

        state.setButton(GamepadInputs.LEFT_STICK_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_LEFT_STICK));
        state.setButton(GamepadInputs.RIGHT_STICK_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_RIGHT_STICK));

        // Additional inputs
        state.setButton(GamepadInputs.MISC_1_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_MISC1));
        state.setButton(GamepadInputs.MISC_2_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_MISC2));
        state.setButton(GamepadInputs.MISC_3_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_MISC3));
        state.setButton(GamepadInputs.MISC_4_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_MISC4));
        state.setButton(GamepadInputs.MISC_5_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_MISC5));
        state.setButton(GamepadInputs.MISC_6_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_MISC6));

        state.setButton(GamepadInputs.LEFT_PADDLE_1_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_LEFT_PADDLE1));
        state.setButton(GamepadInputs.LEFT_PADDLE_2_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_LEFT_PADDLE2));
        state.setButton(GamepadInputs.RIGHT_PADDLE_1_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1));
        state.setButton(GamepadInputs.RIGHT_PADDLE_2_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2));
        state.setButton(GamepadInputs.TOUCHPAD_1_BUTTON, SDL_GetGamepadButton(ptrController, SDL_GAMEPAD_BUTTON_TOUCHPAD));

        this.inputComponent.pushState(state);
    }
    
    private void updateGyro() {
        if (!isGryoSupported) return;

        float[] gyro = new float[3];

        try (Memory memory = new Memory(gyro.length * Float.BYTES)) {
            if (SDL_GetGamepadSensorData(ptrController, SDL_SENSOR_GYRO, memory, 3)) {
                memory.read(0, gyro, 0, gyro.length);

                this.gyroComponent.setState(
                        new GyroState(gyro[0], gyro[1], gyro[2])
                );
            } else {
                CUtil.LOGGER.error("Could not get gyro data: {}", SDL_GetError());
            }
        }
    }

    private void updateTouchpad() {
        if (numTouchpads < 1) return;

        for (int touchpadIdx = 0; touchpadIdx < numTouchpads; touchpadIdx++) {
            Touchpads.Touchpad touchpad = this.touchpadComponent.touchpads()[touchpadIdx];

            List<Touchpads.Finger> fingers = new ArrayList<>();
            for (int fingerIdx = 0; fingerIdx < touchpad.maxFingers(); fingerIdx++) {
                var fingerState = new ByteByReference();
                var x = new FloatByReference();
                var y = new FloatByReference();
                var pressure = new FloatByReference();

                if (!SDL_GetGamepadTouchpadFinger(ptrController, touchpadIdx, fingerIdx, fingerState, x, y, pressure)) {
                    CUtil.LOGGER.error("Failed to fetch touchpad finger: {}", SDL_GetError());
                } else if (fingerState.getValue() == 1) {
                    fingers.add(
                            new Touchpads.Finger(
                                    fingerIdx,
                                    // SDL already returns the correct range for touchpad position and pressure
                                    new Vector2f(x.getValue(), y.getValue()),
                                    pressure.getValue()
                            )
                    );
                }
            }

            touchpad.pushFingers(fingers);
        }
    }

    @Override
    protected SDL_PropertiesID SDL_GetControllerProperties(SDL_Gamepad ptrController) {
        return SDL_GetGamepadProperties(ptrController);
    }

    @Override
    protected String SDL_GetControllerName(SDL_Gamepad ptrController) {
        return SDL_GetGamepadName(ptrController);
    }

    @Override
    protected SDL_JoystickGUID SDL_GetControllerGUIDForID(SDL_JoystickID jid) {
        return SDL_GetGamepadGUIDForID(jid);
    }

    @Override
    protected String SDL_GetControllerSerial(SDL_Gamepad ptrController) {
        return SDL_GetGamepadSerial(ptrController);
    }

    @Override
    protected short SDL_GetControllerVendor(SDL_Gamepad ptrController) {
        return (short) SDL_GetGamepadVendor(ptrController);
    }

    @Override
    protected short SDL_GetControllerProduct(SDL_Gamepad ptrController) {
        return (short) SDL_GetGamepadProduct(ptrController);
    }

    @Override
    protected int SDL_GetControllerConnectionState(SDL_Gamepad ptrController) {
        return SDL_GetGamepadConnectionState(ptrController);
    }

    @Override
    protected boolean SDL_CloseController(SDL_Gamepad ptrController) {
        return SDL_CloseGamepad(ptrController);
    }

    @Override
    protected boolean SDL_RumbleController(SDL_Gamepad ptrController, float strong, float weak, int durationMs) {
        return SDL_RumbleGamepad(ptrController, (char) (strong * 0xFFFF), (char) (weak * 0xFFFF), durationMs);
    }

    @Override
    protected boolean SDL_RumbleControllerTriggers(SDL_Gamepad ptrController, float left, float right, int durationMs) {
        return SDL_RumbleGamepadTriggers(ptrController, (char) (left * 0xFFFF), (char) (right * 0xFFFF), durationMs);
    }

    @Override
    protected int SDL_GetControllerPowerInfo(SDL_Gamepad ptrController, IntByReference percent) {
        return SDL_GetGamepadPowerInfo(ptrController, percent);
    }

    @Override
    protected boolean SDL_SendControllerEffect(SDL_Gamepad ptrController, Pointer effect, int size) {
        return SDL_SendGamepadEffect(ptrController, effect, size);
    }
}
