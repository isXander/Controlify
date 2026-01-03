package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.battery.PowerState;
import dev.isxander.controlify.controller.dualsense.DS5Effect;
import dev.isxander.controlify.controller.dualsense.DualSenseComponent;
import dev.isxander.controlify.controller.haptic.HapticComponent;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.info.DriverNameComponent;
import dev.isxander.controlify.controller.info.GUIDComponent;
import dev.isxander.controlify.controller.info.UIDComponent;
import dev.isxander.controlify.controller.led.LEDComponent;
import dev.isxander.controlify.controller.misc.BluetoothDeviceComponent;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controller.rumble.TriggerRumbleComponent;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.controller.dualsense.DS5EffectsState;
import dev.isxander.controlify.haptics.hd.HDHapticsBus;
import dev.isxander.controlify.haptics.hd.PcmFormat;
import dev.isxander.controlify.haptics.rumble.RumbleState;
import dev.isxander.controlify.haptics.rumble.TriggerRumbleState;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl3java.api.audio.SDL_AudioDeviceID;
import dev.isxander.sdl3java.api.audio.SDL_AudioSpec;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickConnectionState;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickGUID;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.api.power.SDL_PowerState;
import dev.isxander.sdl3java.api.properties.SDL_PropertiesID;
import net.minecraft.util.Util;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.isxander.sdl3java.api.audio.SdlAudio.*;
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
    protected HapticComponent hapticComponent;
    protected LEDComponent ledComponent;
    protected DualSenseComponent dualSenseComponent;
    
    protected final boolean isRumbleSupported, isTriggerRumbleSupported;
    protected final boolean isDualsense;
    protected final boolean isRGBLedSupported;
    
    protected final SDL_JoystickGUID guid;
    protected final String guidString;
    protected final @Nullable String serial;
    protected final String name;
    protected final SDL_PropertiesID props;
    protected final short vendorId, productId;
    protected final SDLJoystickConnectionState connectionState;

    @Nullable
    protected final SDLHapticsOutput hapticsOutput;
    
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

        this.connectionState = SDLJoystickConnectionState.fromInt(SDL_GetControllerConnectionState(ptrController));
        logger.debugLog("SDL Connection State: {}", connectionState);
        
        this.isRumbleSupported = SDL_GetBooleanProperty(props, SDL_PROP_GAMEPAD_CAP_RUMBLE_BOOLEAN, false);
        this.isTriggerRumbleSupported = SDL_GetBooleanProperty(props, SDL_PROP_GAMEPAD_CAP_TRIGGER_RUMBLE_BOOLEAN, false);
        this.isRGBLedSupported = SDL_GetBooleanProperty(props, SDL_PROP_GAMEPAD_CAP_RGB_LED_BOOLEAN, false);

        DecodedGUID decodedGuid = DecodedGUID.fromGUID(this.guid);
        logger.log("SDL GUID driver signature: {}", decodedGuid.getDriverHint());

        if (CUtil.rl("dualsense").equals(type.namespace()) && vendorId == 0x054C) {
            logger.debugLog("DualSense controller detected.");

            this.isDualsense = true;
            this.hapticsOutput = createHapticsOutput();
        } else {
            this.isDualsense = false;
            this.hapticsOutput = null;
        }
    }

    private @Nullable SDLHapticsOutput createHapticsOutput() {
        // macOS HD haptics are broken
        if (Util.getPlatform() != Util.OS.OSX && SDLNativesLoader.get().orElseThrow().hasAudio()) {
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
                var devId = SDL_OpenAudioDevice(dualsenseAudioDev, devSpec);

                var busFormat = new PcmFormat(48_000, 2);
                int chunkFrames = (busFormat.sampleRate() * 20) / 1000; // 20ms
                int ringFrames = (busFormat.sampleRate() * 500) / 1000; // 500ms
                var bus = new HDHapticsBus(busFormat, ringFrames, chunkFrames);

                return new SDLHapticsOutput(devId, devSpec, bus, 80, SDLHapticsOutput.DUALSENSE_CHANNEL_MAP);
            } else {
                logger.debugLog("DualSense HD Haptics audio device not found.");
            }
        }
        return null;
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
        if (this.isRGBLedSupported) {
            controller.setComponent(this.ledComponent = new LEDComponent(1));
        }
        if (this.isDualsense) {
            controller.setComponent(this.dualSenseComponent = new DualSenseComponent());
        }
        if (this.hapticsOutput != null) {
            controller.setComponent(this.hapticComponent = new HapticComponent(this.hapticsOutput.bus().mixer(), this::updateHDHaptic));
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
        updateLED();
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
        
        if (this.hapticsOutput != null) {
            this.hapticsOutput.close();
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

    private void updateLED() {
        if (ledComponent == null) return;

        if (ledComponent.consumeDirty()) {
            int color = ledComponent.get(0); // SDL only supports one LED

            byte red = (byte) ((color >> 16) & 0xFF);
            byte green = (byte) ((color >> 8) & 0xFF);
            byte blue = (byte) (color & 0xFF);

            if (!SDL_SetControllerLED(ptrController, red, green, blue)) {
                logger.error("Could not set controller LED: {}", SDL_GetError());
            } else {
                logger.debugLog("Set controller LED to color: R={}, G={}, B={}", red, green, blue);
            }
        }
    }

    private void updateDualSense() {
        if (dualSenseComponent == null) return;

        DS5EffectsState.ByValue effectsState = new DS5EffectsState.ByValue();

        var queue = this.dualSenseComponent.getEffectQueue();
        boolean touched = false;
        while (!queue.isEmpty()) {
            touched = true;
            DS5Effect effect = queue.poll();
            effect.apply(effectsState);
        }

        if (touched) {
            effectsState.write();
            SDL_SendControllerEffect(ptrController, effectsState.getPointer(), Native.getNativeSize(DS5EffectsState.ByValue.class));
        }
    }

    private void updateHDHaptic() {
        if (this.hapticsOutput == null) return;

        this.hapticsOutput.pump();
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
        return connectionState == SDLJoystickConnectionState.WIRELESS;
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
    protected abstract boolean SDL_SetControllerLED(SDL_Controller ptrController, byte red, byte green, byte blue);
}
