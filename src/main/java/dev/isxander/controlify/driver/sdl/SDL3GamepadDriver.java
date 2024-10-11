package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import dev.isxander.controlify.controller.battery.PowerState;
import dev.isxander.controlify.controller.haptic.HapticEffects;
import dev.isxander.controlify.controller.haptic.SimpleHapticComponent;
import dev.isxander.controlify.controller.haptic.HDHapticComponent;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.dualsense.DualSenseComponent;
import dev.isxander.controlify.controller.haptic.HDHapticComponent;
import dev.isxander.controlify.controller.haptic.HapticBufferLibrary;
import dev.isxander.controlify.controller.misc.BluetoothDeviceComponent;
import dev.isxander.controlify.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controller.rumble.TriggerRumbleComponent;
import dev.isxander.controlify.controller.touchpad.Touchpads;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.rumble.TriggerRumbleState;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.sdl3java.api.audio.*;
import dev.isxander.sdl3java.api.gamepad.SDL_Gamepad;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.api.properties.SDL_PropertiesID;
import dev.isxander.sdl3java.api.sensor.SDL_SensorType;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.HexFormat;

import static dev.isxander.controlify.utils.CUtil.*;
import static dev.isxander.sdl3java.api.SDL_bool.*;
import static dev.isxander.sdl3java.api.audio.SdlAudio.*;
import static dev.isxander.sdl3java.api.audio.SdlAudioConsts.*;
import static dev.isxander.sdl3java.api.error.SdlError.*;
import static dev.isxander.sdl3java.api.events.SdlEventsConst.*;
import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadAxis.*;
import static dev.isxander.sdl3java.api.gamepad.SDL_GamepadButton.*;
import static dev.isxander.sdl3java.api.gamepad.SdlGamepad.*;
import static dev.isxander.sdl3java.api.gamepad.SdlGamepadPropsConst.*;
import static dev.isxander.sdl3java.api.power.SDL_PowerState.*;
import static dev.isxander.sdl3java.api.properties.SdlProperties.*;
import static dev.isxander.sdl3java.api.sensor.SDL_SensorType.*;

public class SDL3GamepadDriver implements Driver {
    private static final int AUDIO_STREAM_TIMEOUT_TICKS = 5 * 60 * 60 * 20; // 5 minutes

    private SDL_Gamepad ptrGamepad;

    private InputComponent inputComponent;
    private BatteryLevelComponent batteryLevelComponent;
    private GyroComponent gyroComponent;
    private RumbleComponent rumbleComponent;
    private TriggerRumbleComponent triggerRumbleComponent;
    private TouchpadComponent touchpadComponent;
    private HDHapticComponent hdHapticComponent;
    private BluetoothDeviceComponent bluetoothDeviceComponent;
    private DualSenseComponent dualSenseComponent;

    private final boolean isGryoSupported;
    private final boolean isRumbleSupported, isTriggerRumbleSupported;
    private final boolean isDualSense;

    private final int numTouchpads;

    private final String guid;
    private final String serial;
    private final String name;

    @Nullable
    private SDL_AudioDeviceID dualsenseAudioDev;
    @Nullable
    private SDL_AudioSpec dualsenseAudioSpec;
    private final List<AudioStreamHandle> dualsenseAudioHandles;

    public SDL3GamepadDriver(SDL_Gamepad ptrGamepad, SDL_JoystickID jid, ControllerType type) {
        this.ptrGamepad = ptrGamepad;

        SDL_PropertiesID properties = SDL_GetGamepadProperties(ptrGamepad);

        this.name = SDL_GetGamepadName(ptrGamepad);
        this.guid = SDL_GetGamepadInstanceGUID(jid).toString();
        this.serial = SDL_GetGamepadSerial(ptrGamepad);

        this.isGryoSupported = SDL_GamepadHasSensor(ptrGamepad, SDL_SensorType.SDL_SENSOR_GYRO) == SDL_TRUE;
        this.isRumbleSupported = SDL_GetBooleanProperty(properties, SDL_PROP_GAMEPAD_CAP_RUMBLE_BOOLEAN, false) == SDL_TRUE;
        this.isTriggerRumbleSupported = SDL_GetBooleanProperty(properties, SDL_PROP_GAMEPAD_CAP_TRIGGER_RUMBLE_BOOLEAN, false) == SDL_TRUE;
        this.numTouchpads = SDL_GetNumGamepadTouchpads(ptrGamepad);

        // open audio device for dualsense hd haptics
        this.dualsenseAudioHandles = new ArrayList<>();

        if (CUtil.rl("dualsense").equals(type.namespace())) {
            this.isDualSense = true;

            // macOS HD haptics are broken
            if (Util.getPlatform() != Util.OS.OSX) {
                SDL_AudioDeviceID dualsenseAudioDev = null;
                SDL_AudioSpec.ByReference devSpec = new SDL_AudioSpec.ByReference();

                for (SDL_AudioDeviceID dev : SDL_GetAudioOutputDevices()) {
                    String name = SDL_GetAudioDeviceName(dev).toLowerCase();
                    if (name.contains("dualsense") || name.contains("ps5") || name.contains("wireless controller")) {
                        SDL_GetAudioDeviceFormat(dev, devSpec, null);
                        if (devSpec.channels == 4) {
                            dualsenseAudioDev = dev;
                            break;
                        }
                    }
                }

                if (dualsenseAudioDev != null) {
                    this.dualsenseAudioSpec = devSpec;
                    this.dualsenseAudioDev = SDL_OpenAudioDevice(dualsenseAudioDev, (SDL_AudioSpec.ByReference) this.dualsenseAudioSpec);
                }
            }
        } else {
            this.isDualSense = false;
        }

        if (this.isGryoSupported) {
            SDL_SetGamepadSensorEnabled(ptrGamepad, SDL_SensorType.SDL_SENSOR_GYRO, true);
        }

    }

    @Override
    public void addComponents(ControllerEntity controller) {
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

        controller.setComponent(this.batteryLevelComponent = new BatteryLevelComponent());

        if (this.isGryoSupported) {
            controller.setComponent(this.gyroComponent = new GyroComponent());
        }

        if (this.isRumbleSupported) {
            controller.setComponent(this.rumbleComponent = new RumbleComponent());
        }

        if (this.isTriggerRumbleSupported) {
            controller.setComponent(this.triggerRumbleComponent = new TriggerRumbleComponent());
        }

        if (this.numTouchpads > 0) {
            controller.setComponent(this.touchpadComponent = new TouchpadComponent(
                    new Touchpads(
                            IntStream.range(0, numTouchpads)
                                .mapToObj(i ->
                                        new Touchpads.Touchpad(
                                                SDL_GetNumGamepadTouchpadFingers(ptrGamepad, i)
                                        )
                                ).toArray(Touchpads.Touchpad[]::new)
                    )
            ));
        }

        if (this.isDualSense) {
            controller.setComponent(this.dualSenseComponent = new DualSenseComponent());
        }

        if (this.dualsenseAudioDev != null) {
            this.hdHapticComponent = new HDHapticComponent();
            this.hdHapticComponent.acceptPlayHaptic(this::playHaptic);
            controller.setComponent(this.hdHapticComponent);

            SimpleHapticComponent simpleHapticComponent = new SimpleHapticComponent();
            simpleHapticComponent.applyOnHaptic(() -> this.hdHapticComponent.playHaptic(HapticEffects.NAVIGATE));
        } else if (this.isDualSense) {
            controller.setComponent(this.bluetoothDeviceComponent = new BluetoothDeviceComponent());
        }
    }

    @Override
    public String getDriverName() {
        return this.name;
    }

    @Override
    public void update(ControllerEntity controller, boolean outOfFocus) {
        if (ptrGamepad == null) {
            throw new IllegalStateException("Tried to update controller even though it's closed.");
        }

        this.updateInput();
        this.updateRumble();
        this.updateGyro();
        this.updateTouchpad();
        this.updateBatteryLevel();
        this.updateHDHaptic();
        this.updateDualSense();
    }

    @Override
    public void close() {
        if (ptrGamepad == null) {
            throw new IllegalStateException("Gamepad already closed.");
        }

        SDL_CloseGamepad(ptrGamepad);
        ptrGamepad = null;

        if (dualsenseAudioDev != null) {
            SDL_CloseAudioDevice(dualsenseAudioDev);
        }
        for (AudioStreamHandle handle : dualsenseAudioHandles) {
            handle.close();
        }
    }

    private void updateInput() {
        ControllerStateImpl state = new ControllerStateImpl();
        // Axis values are in the range [-32768, 32767] (short)
        // https://wiki.libsdl.org/SDL3/SDL_GameControllerGetAxis
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_RIGHT, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_LEFTX))));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_LEFT, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_LEFTX))));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_UP, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_LEFTY))));
        state.setAxis(GamepadInputs.LEFT_STICK_AXIS_DOWN, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_LEFTY))));

        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_RIGHT, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_RIGHTX))));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_LEFT, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_RIGHTX))));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_UP, negativeAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_RIGHTY))));
        state.setAxis(GamepadInputs.RIGHT_STICK_AXIS_DOWN, positiveAxis(mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_RIGHTY))));

        // Triggers are in the range [0, 32767] (thanks SDL!)
        state.setAxis(GamepadInputs.LEFT_TRIGGER_AXIS, mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_LEFT_TRIGGER)));
        state.setAxis(GamepadInputs.RIGHT_TRIGGER_AXIS, mapShortToFloat(SDL_GetGamepadAxis(ptrGamepad, SDL_GAMEPAD_AXIS_RIGHT_TRIGGER)));

        state.setButton(GamepadInputs.SOUTH_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_SOUTH) == SDL_PRESSED);
        state.setButton(GamepadInputs.EAST_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_EAST) == SDL_PRESSED);
        state.setButton(GamepadInputs.WEST_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_WEST) == SDL_PRESSED);
        state.setButton(GamepadInputs.NORTH_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_NORTH) == SDL_PRESSED);

        state.setButton(GamepadInputs.LEFT_SHOULDER_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_LEFT_SHOULDER) == SDL_PRESSED);
        state.setButton(GamepadInputs.RIGHT_SHOULDER_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER) == SDL_PRESSED);

        state.setButton(GamepadInputs.BACK_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_BACK) == SDL_PRESSED);
        state.setButton(GamepadInputs.START_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_START) == SDL_PRESSED);
        state.setButton(GamepadInputs.GUIDE_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_GUIDE) == SDL_PRESSED);

        state.setButton(GamepadInputs.DPAD_UP_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_DPAD_UP) == SDL_PRESSED);
        state.setButton(GamepadInputs.DPAD_DOWN_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_DPAD_DOWN) == SDL_PRESSED);
        state.setButton(GamepadInputs.DPAD_LEFT_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_DPAD_LEFT) == SDL_PRESSED);
        state.setButton(GamepadInputs.DPAD_RIGHT_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_DPAD_RIGHT) == SDL_PRESSED);

        state.setButton(GamepadInputs.LEFT_STICK_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_LEFT_STICK) == SDL_PRESSED);
        state.setButton(GamepadInputs.RIGHT_STICK_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_RIGHT_STICK) == SDL_PRESSED);

        // Additional inputs
        state.setButton(GamepadInputs.MISC_1_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_MISC1) == SDL_PRESSED);
        state.setButton(GamepadInputs.MISC_2_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_MISC2) == SDL_PRESSED);
        state.setButton(GamepadInputs.MISC_3_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_MISC3) == SDL_PRESSED);
        state.setButton(GamepadInputs.MISC_4_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_MISC4) == SDL_PRESSED);
        state.setButton(GamepadInputs.MISC_5_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_MISC5) == SDL_PRESSED);
        state.setButton(GamepadInputs.MISC_6_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_MISC6) == SDL_PRESSED);

        state.setButton(GamepadInputs.LEFT_PADDLE_1_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_LEFT_PADDLE1) == SDL_PRESSED);
        state.setButton(GamepadInputs.LEFT_PADDLE_2_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_LEFT_PADDLE2) == SDL_PRESSED);
        state.setButton(GamepadInputs.RIGHT_PADDLE_1_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1) == SDL_PRESSED);
        state.setButton(GamepadInputs.RIGHT_PADDLE_2_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2) == SDL_PRESSED);
        state.setButton(GamepadInputs.TOUCHPAD_1_BUTTON, SDL_GetGamepadButton(ptrGamepad, SDL_GAMEPAD_BUTTON_TOUCHPAD) == SDL_PRESSED);

        this.inputComponent.pushState(state);
    }

    private void updateRumble() {
        if (isRumbleSupported) {
            Optional<RumbleState> stateOpt = this.rumbleComponent.consumeRumble();

            stateOpt.ifPresent(state -> {
                if (SDL_RumbleGamepad(ptrGamepad, (short)(state.strong() * 0xFFFF), (short)(state.weak() * 0xFFFF), 5000) != 0) {
                    CUtil.LOGGER.error("Could not rumble gamepad: {}", SDL_GetError());
                }
            });
        }

        if (isTriggerRumbleSupported) {
            Optional<TriggerRumbleState> stateOpt = this.triggerRumbleComponent.consumeTriggerRumble();

            stateOpt.ifPresent(state -> {
                if (SDL_RumbleGamepadTriggers(ptrGamepad, (short)(state.left() * 0xFFFF), (short)(state.right() * 0xFFFF), 0) != 0) {
                    CUtil.LOGGER.error("Could not rumble triggers gamepad: {}", SDL_GetError());
                }
            });
        }
    }

    private void updateGyro() {
        if (!isGryoSupported) return;

        float[] gyro = new float[3];

        try (Memory memory = new Memory(gyro.length * Float.BYTES)) {
            if (SDL_GetGamepadSensorData(ptrGamepad, SDL_SENSOR_GYRO, memory, 3) == 0) {
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

                if (SDL_GetGamepadTouchpadFinger(ptrGamepad, touchpadIdx, fingerIdx, fingerState, x, y, pressure) != 0) {
                    CUtil.LOGGER.error("Failed to fetch touchpad finger: {}", SDL_GetError());
                } else if (fingerState.getValue() == SDL_PRESSED) {
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

    private void updateBatteryLevel() {
        IntByReference percent = new IntByReference();
        int powerState = SDL_GetGamepadPowerInfo(ptrGamepad, percent);

        PowerState level = switch (powerState) {
            case SDL_POWERSTATE_ERROR, SDL_POWERSTATE_UNKNOWN -> new PowerState.Unknown();
            case SDL_POWERSTATE_ON_BATTERY -> new PowerState.Depleting(percent.getValue());
            case SDL_POWERSTATE_NO_BATTERY -> new PowerState.WiredOnly();
            case SDL_POWERSTATE_CHARGING -> new PowerState.Charging(percent.getValue());
            case SDL_POWERSTATE_CHARGED -> new PowerState.Full();
            default -> throw new IllegalStateException("Unexpected value");
        };

        this.batteryLevelComponent.setBatteryLevel(level);
    }

    private void updateDualSense() {
        if (dualSenseComponent == null) return;

        DS5EffectsState.ByValue effectsState = new DS5EffectsState.ByValue();
        boolean somethingHappened = false;

        if (this.dualSenseComponent.consumeMuteLightDirty()) {
            somethingHappened = true;

            effectsState.ucEnableBits2 |= DS5EffectsState.EnableBitFlags2.ALLOW_MUTE_LIGHT;
            if (this.dualSenseComponent.getMuteLight()) {
                effectsState.ucMicLightMode = 1;
            }
        }

        if (somethingHappened) {
            effectsState.write();

            SDL_SendGamepadEffect(ptrGamepad, effectsState.getPointer(), Native.getNativeSize(DS5EffectsState.ByValue.class));
        }
    }

    private void updateHDHaptic() {
        for (int i = 0; i < dualsenseAudioHandles.size(); i++) {
            AudioStreamHandle handle = dualsenseAudioHandles.get(i);
            if (handle.isTimedOut()) {
                handle.close();
                dualsenseAudioHandles.remove(handle);
            } else {
                handle.tick();
            }
        }
    }

    private void playHaptic(HapticBufferLibrary.HapticBuffer sound) {
        if (ptrGamepad == null || dualsenseAudioDev == null || dualsenseAudioSpec == null) {
            return;
        }

        SDL_AudioSpec spec = new SDL_AudioSpec();
        spec.channels = sound.format().getChannels();
        spec.freq = (int) sound.format().getSampleRate();

        int ss = sound.format().getSampleSizeInBits();
        int byteSs = ss / 8;
        AudioFormat.Encoding encoding = sound.format().getEncoding();
        if (ss == 8) {
            if (encoding == AudioFormat.Encoding.PCM_SIGNED) {
                spec.format = new SDL_AudioFormat(SDL_AUDIO_S8);
            } else if (encoding == AudioFormat.Encoding.PCM_UNSIGNED) {
                spec.format = new SDL_AudioFormat(SDL_AUDIO_U8);
            }
        } else if (sound.format().isBigEndian()) {
            audioFmtEndian(spec, ss, encoding, SDL_AUDIO_S16BE, SDL_AUDIO_S32BE, SDL_AUDIO_F32BE);
        } else {
            audioFmtEndian(spec, ss, encoding, SDL_AUDIO_S16LE, SDL_AUDIO_S32LE, SDL_AUDIO_F32LE);
        }

        if (spec.format == null) {
            throw new IllegalStateException("Unsupported format");
        }

        AudioStreamHandle handle = null;
        for (AudioStreamHandle stream : dualsenseAudioHandles) {
            SDL_AudioSpec streamSpec = stream.getSpec();
            if (streamSpec.format.intValue() == spec.format.intValue()
                    && streamSpec.freq == spec.freq
                    && streamSpec.channels == spec.channels
                    && !stream.isInUse()
            ) {
                handle = stream;
                break;
            }
        }
        int length = sound.audio().length / spec.freq / spec.channels / byteSs * 20;

        if (handle != null) {
            handle.queueAudio(sound.audio(), length);
        } else {
            if (dualsenseAudioHandles.size() >= 16) {
                dualsenseAudioHandles.remove(0).close();
            }

            AudioStreamHandle newHandle = AudioStreamHandle.createWithAudio(dualsenseAudioDev, spec, dualsenseAudioSpec, sound.audio(), length);
            dualsenseAudioHandles.add(newHandle);
        }
    }

    private static void audioFmtEndian(SDL_AudioSpec spec, int ss, AudioFormat.Encoding encoding, int signed16, int signed32, int float32) {
        if (ss == 16) {
            if (encoding == AudioFormat.Encoding.PCM_SIGNED) {
                spec.format = new SDL_AudioFormat(signed16);
            }
        } else if (ss == 32) {
            if (encoding == AudioFormat.Encoding.PCM_SIGNED) {
                spec.format = new SDL_AudioFormat(signed32);
            } else if (encoding == AudioFormat.Encoding.PCM_FLOAT) {
                spec.format = new SDL_AudioFormat(float32);
            }
        }
    }

    private static class AudioStreamHandle {
        private int streamLastPlayed;
        private final SDL_AudioStream stream;
        private final SDL_AudioSpec spec;

        private AudioStreamHandle(SDL_AudioStream stream, SDL_AudioSpec spec) {
            this.stream = stream;
            this.spec = spec;
            this.streamLastPlayed = 0;
        }

        public void queueAudio(byte[] audio, int tickLength) {
            try (Memory memory = new Memory(audio.length)) {
                memory.write(0, audio, 0, audio.length);
                SDL_PutAudioStreamData(stream, memory, audio.length);

                streamLastPlayed = Math.min(0, streamLastPlayed);
                streamLastPlayed -= tickLength;
            }
        }

        public SDL_AudioSpec getSpec() {
            return this.spec;
        }

        public boolean isInUse() {
            return streamLastPlayed < 0;
        }

        public boolean isTimedOut() {
            return streamLastPlayed >= AUDIO_STREAM_TIMEOUT_TICKS;
        }

        public void tick() {
            streamLastPlayed++;
        }

        public void close() {
            SDL_DestroyAudioStream(stream);
        }

        public static AudioStreamHandle createWithAudio(SDL_AudioDeviceID device, SDL_AudioSpec audioSpec, SDL_AudioSpec devSpec, byte[] audio, int tickLength) {
            SDL_AudioStream stream = SDL_CreateAudioStream(audioSpec, devSpec);
            SDL_BindAudioStream(device, stream);

            var handle = new AudioStreamHandle(stream, audioSpec);
            handle.queueAudio(audio, tickLength);
            return handle;
        }

        public static AudioStreamHandle createWithInitialTimeout(SDL_AudioDeviceID device, SDL_AudioSpec srcSpec, SDL_AudioSpec devSpec) {
            SDL_AudioStream stream = SDL_CreateAudioStream(srcSpec, devSpec);
            SDL_BindAudioStream(device, stream);

            return new AudioStreamHandle(stream, srcSpec);
        }
    }
}
