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
import dev.isxander.controlify.controller.touchpad.Touchpads;
import dev.isxander.controlify.input.InputComponent;
import dev.isxander.controlify.input.InputPipeline;
import dev.isxander.controlify.input.SensorPipeline;
import dev.isxander.controlify.input.input.SensorType;
import dev.isxander.controlify.input.pipeline.Clock;
import dev.isxander.controlify.input.pipeline.EventSource;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl3java.api.gamepad.SDL_Gamepad;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickGUID;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.api.properties.SDL_PropertiesID;
import dev.isxander.sdl3java.api.sensor.SDL_SensorType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static dev.isxander.sdl3java.api.error.SdlError.*;
import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadAxis.*;
import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadButton.*;
import static dev.isxander.sdl3java.api.gamepad.SdlGamepad.*;
import static dev.isxander.sdl3java.api.sensor.SDL_SensorType.*;

public class SdlGamepadDriver extends SdlCommonDriver<SDL_Gamepad> {
    
    private InputComponent inputComponent;
    private GyroComponent gyroComponent;
    private TouchpadComponent touchpadComponent;

    private final boolean isGryoSupported;

    private final int numTouchpads;
    
    public SdlGamepadDriver(SDL_Gamepad ptrController, SDL_JoystickID jid, ControllerType type, EventSource<SdlControllerEvent> eventSource, ControlifyLogger logger) {
        super(ptrController, jid, type, eventSource, logger);
 
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
                        new InputPipeline(this.eventSource.via(new SdlInputEventStage()), Clock.STOPPED), // TODO: Clock
                        new SensorPipeline(this.eventSource.via(new SdlSensorEventStage()), Clock.STOPPED), // TODO: Clock
                        detectSupportedButtons(),
                        detectSupportedAxes(),
                        GamepadInputs.DEADZONE_GROUPS,
                        true,
                        this.isGryoSupported ? Set.of(SensorType.GYROSCOPE) : Set.of()
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

        this.updateGyro();
        this.updateTouchpad();


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

    private Set<ResourceLocation> detectSupportedButtons() {
        var set = HashSet.<ResourceLocation>newHashSet(SDL_GAMEPAD_BUTTON_COUNT);

        for (int btn = 0; btn < SDL_GAMEPAD_BUTTON_COUNT; btn++) {
            if (SDL_GamepadHasButton(ptrController, btn)) {
                set.add(SdlInputConversions.mapGamepadButton(btn));
            }
        }

        return set;
    }

    private Set<ResourceLocation> detectSupportedAxes() {
        var set = HashSet.<ResourceLocation>newHashSet(SDL_GAMEPAD_AXIS_COUNT * 2);

        for (int ax = 0; ax < SDL_GAMEPAD_AXIS_COUNT; ax++) {
            if (SDL_GamepadHasAxis(ptrController, ax)) {
                set.add(SdlInputConversions.mapGamepadAxis(ax, true));
                set.add(SdlInputConversions.mapGamepadAxis(ax, false));
            }
        }

        return set;
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

    @Override
    protected boolean SDL_SetControllerLED(SDL_Gamepad ptrController, byte red, byte green, byte blue) {
        return SDL_SetGamepadLED(ptrController, red, green, blue);
    }
}
