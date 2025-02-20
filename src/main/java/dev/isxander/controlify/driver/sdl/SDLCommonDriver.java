package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.battery.PowerState;
import dev.isxander.controlify.controller.dualsense.DualSenseComponent;
import dev.isxander.controlify.controller.haptic.CompleteSoundData;
import dev.isxander.controlify.controller.haptic.HDHapticComponent;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.info.DriverNameComponent;
import dev.isxander.controlify.controller.info.GUIDComponent;
import dev.isxander.controlify.controller.info.UIDComponent;
import dev.isxander.controlify.controller.misc.BluetoothDeviceComponent;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controller.rumble.TriggerRumbleComponent;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.driver.sdl.dualsense.DS5EffectsState;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.rumble.TriggerRumbleState;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl3java.api.audio.SDL_AudioDeviceID;
import dev.isxander.sdl3java.api.audio.SDL_AudioFormat;
import dev.isxander.sdl3java.api.audio.SDL_AudioSpec;
import dev.isxander.sdl3java.api.audio.SDL_AudioStream;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickConnectionState;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickGUID;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.api.power.SDL_PowerState;
import dev.isxander.sdl3java.api.properties.SDL_PropertiesID;
import net.minecraft.Util;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static dev.isxander.sdl3java.api.audio.SdlAudio.*;
import static dev.isxander.sdl3java.api.audio.SdlAudioConsts.*;
import static dev.isxander.sdl3java.api.error.SdlError.*;
import static dev.isxander.sdl3java.api.gamepad.SdlGamepadPropsConst.*;
import static dev.isxander.sdl3java.api.guid.SdlGuid.*;
import static dev.isxander.sdl3java.api.power.SDL_PowerState.*;
import static dev.isxander.sdl3java.api.properties.SdlProperties.*;

public abstract class SDLCommonDriver<SDL_Controller> implements Driver {
    private static final int AUDIO_STREAM_TIMEOUT_TICKS = 5 * 60 * 60 * 20; // 5 minutes

    private final ControlifyLogger logger;

    protected SDL_Controller ptrController;

    protected BatteryLevelComponent batteryLevelComponent;
    protected RumbleComponent rumbleComponent;
    protected TriggerRumbleComponent triggerRumbleComponent;
    protected HDHapticComponent hdHapticComponent;
    protected DualSenseComponent dualSenseComponent;
    
    protected final boolean isRumbleSupported, isTriggerRumbleSupported;
    protected final boolean isDualsense;
    
    protected final SDL_JoystickGUID guid;
    protected final String guidString;
    protected final @Nullable String serial;
    protected final String name;
    protected final SDL_PropertiesID props;
    protected final short vendorId, productId;
    protected final int connectionState;

    @Nullable
    protected SDL_AudioDeviceID dualsenseAudioDev;
    @Nullable
    protected SDL_AudioSpec dualsenseAudioSpec;
    protected final List<AudioStreamHandle> dualsenseAudioHandles;
    
    public SDLCommonDriver(SDL_Controller ptrController, SDL_JoystickID jid, ControllerType type, ControlifyLogger logger) {
        this.ptrController = ptrController;
        this.logger = logger;
        
        this.props = SDL_GetControllerProperties(ptrController);

        this.name = SDL_GetControllerName(ptrController);

        this.guid = SDL_GetControllerGUIDForID(jid);
        this.guidString = SDL_GUIDToString(guid);
        logger.debugLog("SDL GUID: {}", guidString);

        this.serial = SDL_GetControllerSerial(ptrController);
        logger.debugLog("SDL Serial: {}", serial);

        this.vendorId = SDL_GetControllerVendor(ptrController);
        this.productId = SDL_GetControllerProduct(ptrController);
        logger.debugLog("SDL VID: {} PID: {}", vendorId, productId);

        this.connectionState = SDL_GetControllerConnectionState(ptrController);
        logger.debugLog("SDL Connection State: {}", connectionState);
        
        this.isRumbleSupported = SDL_GetBooleanProperty(props, SDL_PROP_GAMEPAD_CAP_RUMBLE_BOOLEAN, false);
        this.isTriggerRumbleSupported = SDL_GetBooleanProperty(props, SDL_PROP_GAMEPAD_CAP_TRIGGER_RUMBLE_BOOLEAN, false);

        DecodedGUID decodedGuid = DecodedGUID.fromGUID(this.guid);
        logger.log("SDL GUID driver signature: {}", decodedGuid.getDriverHint());

        // open audio device for dualsense hd haptics
        this.dualsenseAudioHandles = new ArrayList<>();

        if (CUtil.rl("dualsense").equals(type.namespace())) {
            this.isDualsense = true;
            logger.debugLog("DualSense controller detected.");

            // macOS HD haptics are broken
            if (Util.getPlatform() != Util.OS.OSX) {
                SDL_AudioDeviceID dualsenseAudioDev = null;
                SDL_AudioSpec.ByReference devSpec = new SDL_AudioSpec.ByReference();

                for (SDL_AudioDeviceID dev : SDL_GetAudioPlaybackDevices()) {
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
                    logger.debugLog("DualSense HD Haptics audio device found.");
                    this.dualsenseAudioSpec = devSpec;
                    this.dualsenseAudioDev = SDL_OpenAudioDevice(dualsenseAudioDev, (SDL_AudioSpec.ByReference) this.dualsenseAudioSpec);
                } else {
                    logger.debugLog("DualSense HD Haptics audio device not found.");
                }
            }
        } else {
            this.isDualsense = false;
        }
    }

    @Override
    public void addComponents(ControllerEntity controller) {
        controller.setComponent(new DriverNameComponent(this.name));
        controller.setComponent(new GUIDComponent(this.guidString));
        controller.setComponent(new UIDComponent(createUid()));

        controller.setComponent(this.batteryLevelComponent = new BatteryLevelComponent());
        if (this.isRumbleSupported) {
            controller.setComponent(this.rumbleComponent = new RumbleComponent());
        }
        if (this.isTriggerRumbleSupported) {
            controller.setComponent(this.triggerRumbleComponent = new TriggerRumbleComponent());
        }
        if (this.isDualsense) {
            controller.setComponent(this.dualSenseComponent = new DualSenseComponent());
        }
        if (this.dualsenseAudioDev != null) {
            controller.setComponent(this.hdHapticComponent = new HDHapticComponent());
            this.hdHapticComponent.acceptPlayHaptic(this::playHaptic);
        }
        
        if (isBluetooth()) {
            controller.setComponent(new BluetoothDeviceComponent());
        }
    }

    @Override
    public void update(ControllerEntity controller, boolean outOfFocus) {
        if (ptrController == null) {
            throw new IllegalStateException("Tried to update controller when it's closed.");
        }

        updateRumble();
        updateBatteryLevel();
        updateDualSense();
        updateHDHaptic();
    }

    @Override
    public void close() {
        if (ptrController == null) {
            throw new IllegalStateException("Tried to close controller when it's already closed.");
        }
        
        SDL_CloseController(ptrController);
        ptrController = null;
        
        if (dualsenseAudioDev != null) {
            SDL_CloseAudioDevice(dualsenseAudioDev);
            dualsenseAudioDev = null;
            for (AudioStreamHandle handle : dualsenseAudioHandles) {
                handle.close();
            }
        }
    }
    
    protected void updateRumble() {
        if (isRumbleSupported) {
            Optional<RumbleState> stateOpt = this.rumbleComponent.consumeRumble();

            stateOpt.ifPresent(state -> {
                if (!SDL_RumbleController(ptrController, state.strong(), state.weak(), 5000)) {
                    CUtil.LOGGER.error("Could not rumble gamepad: {}", SDL_GetError());
                }
            });
        }

        if (isTriggerRumbleSupported) {
            Optional<TriggerRumbleState> stateOpt = this.triggerRumbleComponent.consumeTriggerRumble();

            stateOpt.ifPresent(state -> {
                if (!SDL_RumbleControllerTriggers(ptrController, state.left(), state.right(), 0)) {
                    CUtil.LOGGER.error("Could not rumble triggers gamepad: {}", SDL_GetError());
                }
            });
        }
    }

    private void updateBatteryLevel() {
        IntByReference percent = new IntByReference();
        int powerState = SDL_GetControllerPowerInfo(ptrController, percent);

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

        if (this.dualSenseComponent.consumeDirty()) {
            // Left Trigger Effect
            effectsState.ucEnableBits1 |= DS5EffectsState.EnableBitFlags1.ALLOW_LEFT_TRIGGER_FFB;
            effectsState.rgucLeftTriggerEffect = this.dualSenseComponent.getLeftTriggerEffect();

            // Right Trigger Effect
            effectsState.ucEnableBits1 |= DS5EffectsState.EnableBitFlags1.ALLOW_RIGHT_TRIGGER_FFB;
            effectsState.rgucRightTriggerEffect = this.dualSenseComponent.getRightTriggerEffect();

            // Mute Light
            effectsState.ucEnableBits2 |= DS5EffectsState.EnableBitFlags2.ALLOW_MUTE_LIGHT;
            effectsState.ucMicLightMode = DS5EffectsState.MuteLightState.fromBoolean(this.dualSenseComponent.getMuteLight());

            effectsState.write();

            SDL_SendControllerEffect(ptrController, effectsState.getPointer(), Native.getNativeSize(DS5EffectsState.ByValue.class));
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

    private void playHaptic(CompleteSoundData sound) {
        if (ptrController == null || dualsenseAudioDev == null || dualsenseAudioSpec == null) {
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

    protected String createUid() {
        int identifiers = 0;
        List<byte[]> bytes = new ArrayList<>();

        // IMPORTANT: the order of these identifiers are important, as they are passed through a hash function
        // rearranging the order will result in a different UID

        // add vendor and product id if available
        if (vendorId != 0 && productId != 0) {
            bytes.add(new byte[] {
                    (byte) (vendorId >> 8), (byte) vendorId,
                    (byte) (productId >> 8), (byte) productId
            });
            identifiers++;
        }

        // add serial if available - even with different drivers, serials should remain constant, if provided
        if (this.serial != null) {
            bytes.add(this.serial.getBytes());
            identifiers++;
        }

        if (identifiers == 0) {
            // if no other providers are available, use the GUID
            // the GUID is prone to changing quite a bit, so it's not a good identifier
            bytes.add(this.guid.data.clone());
        }

        String uid = CUtil.createUIDFromBytes(bytes.toArray(new byte[0][]));

        String nonDuplicateUid = uid;
        int duplicateCount = (int) Controlify.instance().getControllerManager().orElseThrow()
                .getConnectedControllers()
                .stream()
                .filter(controller -> controller.uid().startsWith(nonDuplicateUid))
                .count();
        if (duplicateCount > 0) {
            uid += "-" + duplicateCount;
        }

        return uid;
    }

    protected boolean isBluetooth() {
        return connectionState == SDL_JoystickConnectionState.SDL_JOYSTICK_CONNECTION_WIRELESS;
    }

    protected abstract SDL_PropertiesID SDL_GetControllerProperties(SDL_Controller ptrController);
    protected abstract String SDL_GetControllerName(SDL_Controller ptrController);
    protected abstract SDL_JoystickGUID SDL_GetControllerGUIDForID(SDL_JoystickID jid);
    protected abstract @Nullable String SDL_GetControllerSerial(SDL_Controller ptrController);
    protected abstract short SDL_GetControllerVendor(SDL_Controller ptrController);
    protected abstract short SDL_GetControllerProduct(SDL_Controller ptrController);
    @MagicConstant(valuesFromClass = SDL_JoystickConnectionState.class)
    protected abstract int SDL_GetControllerConnectionState(SDL_Controller ptrController);
    protected abstract boolean SDL_CloseController(SDL_Controller ptrController);
    protected abstract boolean SDL_RumbleController(SDL_Controller ptrController, float strong, float weak, int durationMs);
    protected abstract boolean SDL_RumbleControllerTriggers(SDL_Controller ptrController, float left, float right, int durationMs);
    @MagicConstant(valuesFromClass = SDL_PowerState.class)
    protected abstract int SDL_GetControllerPowerInfo(SDL_Controller ptrController, IntByReference percent);
    protected abstract boolean SDL_SendControllerEffect(SDL_Controller ptrController, Pointer effect, int size);

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

    protected static class AudioStreamHandle {
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

            int[] channelMap = switch (audioSpec.channels) {
                case 1 -> new int[]{ -1, -1, 0, 0 };
                case 2 -> new int[]{ -1, -1, 0, 1 };
                default -> throw new IllegalStateException("Unsupported channel count " + audioSpec.channels);
            };
            if (!SDL_SetAudioStreamOutputChannelMap(stream, channelMap)) {
                System.out.println(SDL_GetError());
            }

            var handle = new AudioStreamHandle(stream, audioSpec);
            handle.queueAudio(audio, tickLength);
            return handle;
        }
    }
}
