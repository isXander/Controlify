/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.driver.sdl;

import dev.isxander.controlify.api.controller.dualsense.MicLightMode;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.impl.controller.battery.PowerState;
import dev.isxander.controlify.impl.controller.dualsense.DualsenseComponentImpl;
import dev.isxander.controlify.impl.controller.gyro.GyroComponentImpl;
import dev.isxander.controlify.impl.controller.haptic.PcmHapticsComponent;
import dev.isxander.controlify.impl.controller.haptic.SteamHapticsComponent;
import dev.isxander.controlify.impl.controllermanager.ControllerType;
import dev.isxander.controlify.impl.controller.misc.DriverNameComponent;
import dev.isxander.controlify.impl.controller.misc.UIDComponent;
import dev.isxander.controlify.impl.controller.led.LEDComponent;
import dev.isxander.controlify.impl.controller.misc.BluetoothDeviceComponent;
import dev.isxander.controlify.impl.controller.rumble.RumbleComponentImpl;
import dev.isxander.controlify.impl.controller.rumble.TriggerRumbleComponent;
import dev.isxander.controlify.impl.driver.Driver;
import dev.isxander.controlify.impl.driver.dualsense.DualsenseEffectsState;
import dev.isxander.controlify.impl.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.impl.feedback.PcmSink;
import dev.isxander.controlify.impl.input.pipeline.InputPipeline;
import dev.isxander.controlify.impl.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.controller.rumble.TriggerRumbleState;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.utils.log.ControlifyLogger;
import dev.isxander.sdl.*;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.isxander.sdl.SdlAudio.*;
import static dev.isxander.sdl.SdlGamepad.SDL_SENSOR_ACCEL;
import static dev.isxander.sdl.SdlGamepad.SDL_SENSOR_GYRO;
import static dev.isxander.sdl.SdlInit.*;
import static dev.isxander.sdl.SdlProperties.*;

public abstract class SDLCommonDriver<SdlController> implements Driver {
	/// The most haptic audio written to the stream in one call.
	private static final int HAPTIC_BUFFER_FRAMES = 4096;
	/// How long SDL keeps a rumble going without it being refreshed. The feedback engine resends
	/// active rumble more often than this, so it only matters if the game stops updating.
	private static final int RUMBLE_TIMEOUT_MS = 5000;

	protected final Sdl sdl;
	protected final ControlifyLogger logger;

	protected final Arena arena;

	protected SdlController ptrController;

	protected final EventSource<SDLControllerEvent> sdlEventSource;
	protected final InputPipeline inputPipeline;
	private final SDLToPhysicalStage sdlToPhysicalStage;
	private EventSource.Subscription eventSubscription;

	protected BatteryLevelComponent batteryLevelComponent;
	protected RumbleComponentImpl rumbleComponent;
	protected TriggerRumbleComponent triggerRumbleComponent;
	protected PcmHapticsComponent pcmHapticsComponent;
	protected LEDComponent ledComponent;
	protected DualsenseComponentImpl dualSenseComponent;

	protected final boolean isRumbleSupported, isTriggerRumbleSupported;
	protected final boolean isDualsense;
	protected final boolean isRGBLedSupported;
	protected final boolean isGyroSupported;
	protected final boolean isAccelSupported;

	protected final SdlGuid guid;
	protected final String guidString;
	protected final @Nullable String serial;
	protected final @NotNull String name;
	protected final SdlPropertiesId props;
	protected final short vendorId, productId;
	protected final SDLJoystickConnectionState connectionState;
	protected final ControllerType controllerType;

	@Nullable protected SdlAudioDeviceId dualsenseAudioDev;
	@Nullable protected SdlAudioSpec dualsenseAudioSpec;
	/// Stereo float audio that drives the left and right haptic actuators.
	@Nullable private SdlAudioStreamHandle hapticStream;
	@Nullable private MemorySegment hapticBuffer;

	private boolean active;

	public SDLCommonDriver(Sdl sdl, SdlController ptrController, SdlJoystickId jid, EventSource<SDLControllerEvent> eventSource, ControllerType type, ControlifyLogger logger) {
		this.sdl = sdl;
		this.arena = Arena.ofConfined();

		this.ptrController = ptrController;
		this.sdlEventSource = eventSource;
		this.logger = logger;

		this.props = SDL_GetControllerProperties(ptrController);
		this.controllerType = type;

		this.name = Optional.ofNullable(SDL_GetControllerName(ptrController))
			.orElse("SDL Controller");

		this.guid = SDL_GetControllerGUIDForID(jid);
		this.guidString = sdl.guid().SDL_GUIDToString(guid);
		logger.debugLog("SDL GUID: {}", guidString);

		this.serial = SDL_GetControllerSerial(ptrController);
		logger.debugLog("SDL Serial: {}", serial);

		this.vendorId = SDL_GetControllerVendor(ptrController);
		this.productId = SDL_GetControllerProduct(ptrController);
		logger.debugLog("SDL VID: {} PID: {}", vendorId, productId);

		this.connectionState = SDLJoystickConnectionState.fromInt(SDL_GetControllerConnectionState(ptrController));
		logger.debugLog("SDL Connection State: {}", connectionState);

		this.isRumbleSupported = sdl.properties().SDL_GetBooleanProperty(props, SDL_PROP_GAMEPAD_CAP_RUMBLE_BOOLEAN, false);
		this.isTriggerRumbleSupported = sdl.properties().SDL_GetBooleanProperty(props, SDL_PROP_GAMEPAD_CAP_TRIGGER_RUMBLE_BOOLEAN, false);
		this.isRGBLedSupported = sdl.properties().SDL_GetBooleanProperty(props, SDL_PROP_GAMEPAD_CAP_RGB_LED_BOOLEAN, false);
		this.isGyroSupported = SDL_ControllerHasSensor(this.ptrController, SDL_SENSOR_GYRO);
		this.isAccelSupported = SDL_ControllerHasSensor(this.ptrController, SDL_SENSOR_ACCEL);

		DecodedGUID decodedGuid = DecodedGUID.fromGUID(this.guid);
		logger.log("SDL GUID driver signature: {}", decodedGuid.getDriverHint());

		this.inputPipeline = new InputPipeline();
		this.sdlToPhysicalStage = new SDLToPhysicalStage(this.sdl);

		// open audio device for dualsense hd haptics
		this.isDualsense = this.isDualsense();
		if (this.isDualsense) {
			logger.debugLog("DualSense controller detected.");

			// macOS HD haptics are broken
			if (sdl.init().SDL_WasInit(SDL_INIT_AUDIO) != 0) {
				SdlAudioDeviceId dualsenseAudioDev = null;
				SdlAudio.SdlAudioSpecRef devSpec = new SdlAudio.SdlAudioSpecRef();

				String controllerContainer = sdl.properties().SDL_GetStringProperty(props, SDL_PROP_GAMEPAD_CONTAINER_ID_STRING, "");

				for (SdlAudioDeviceId dev : sdl.audio().SDL_GetAudioPlaybackDevices()) {
					SdlPropertiesId audioDevProps = sdl.audio().SDL_GetAudioDeviceProperties(dev);
					String audioContainer = sdl.properties().SDL_GetStringProperty(audioDevProps, SDL_PROP_AUDIO_DEVICE_CONTAINER_ID_STRING, "");

					if (!"".equals(controllerContainer) && !"".equals(audioContainer) && Objects.equals(controllerContainer, audioContainer)) {
						if (!sdl.audio().SDL_GetAudioDeviceFormat(dev, devSpec, new SdlRefs.IntRef())) {
							logger.error("Failed to get candidate audio device format: {}", sdl.error().SDL_GetError());
							continue;
						}

						if (devSpec.value.channels() == 4) {
							dualsenseAudioDev = dev;
							break;
						}
					}
				}

				if (dualsenseAudioDev != null) {
					logger.debugLog("DualSense HD Haptics audio device found.");
					this.dualsenseAudioSpec = devSpec.value;
					this.dualsenseAudioDev = sdl.audio().SDL_OpenAudioDevice(dualsenseAudioDev, this.dualsenseAudioSpec);
					this.openHapticStream();
				} else {
					logger.debugLog("DualSense HD Haptics audio device not found.");
				}
			}
		}
	}

	@Override
	public void addComponents(ControllerImpl controller) {
		controller.addComponent(new DriverNameComponent(this.name));
		controller.addComponent(new UIDComponent(createUid()));

		controller.addComponent(this.batteryLevelComponent = new BatteryLevelComponent());
		if (this.isRumbleSupported) {
			controller.addComponent(this.rumbleComponent = new RumbleComponentImpl());
		}
		if (this.isTriggerRumbleSupported) {
			controller.addComponent(this.triggerRumbleComponent = new TriggerRumbleComponent());
		}
		if (this.isRGBLedSupported) {
			controller.addComponent(this.ledComponent = new LEDComponent());
		}
		if (this.isDualsense) {
			controller.addComponent(this.dualSenseComponent = new DualsenseComponentImpl());
		}
		if (this.isGyroSupported) {
			controller.addComponent(new GyroComponentImpl(this.inputPipeline));
		}
		if (this.isSteamTriton()) {
			controller.addComponent(new SteamHapticsComponent(this::sendRawEffect));
		}
		if (this.hapticStream != null) {
			controller.addComponent(this.pcmHapticsComponent = new PcmHapticsComponent(new HapticSink()));
		}

		if (isBluetooth()) {
			controller.addComponent(new BluetoothDeviceComponent());
		}

		// SDL only sends battery events on change, so read the level that is already known at connect
		SdlRefs.IntRef percent = new SdlRefs.IntRef();
		var initialState = SDLInputConversions.getPowerState(this.SDL_GetControllerPowerInfo(this.ptrController, percent));
		if (initialState != null) {
			this.applyBatteryState(initialState, percent.value);
		}

		this.eventSubscription = this.sdlEventSource.subscribe(this::onEvent);
	}

	protected void onEvent(SDLControllerEvent event) {
		this.sdlToPhysicalStage.onEvent(event, this.inputPipeline);

		switch (event) {
			case SDLControllerEvent.Battery battery ->
					this.onBatteryEvent(battery);

			default -> {}
		}
	}

	private void onBatteryEvent(SDLControllerEvent.Battery event) {
		this.applyBatteryState(event.state(), event.percent());
	}

	private void applyBatteryState(SDLControllerEvent.Battery.PowerState state, int percent) {
		PowerState level = switch (state) {
			case ERROR, UNKNOWN -> new PowerState.Unknown();
			case ON_BATTERY -> percent < 0 ? new PowerState.Unknown() : new PowerState.Depleting(percent);
			case NO_BATTERY -> new PowerState.WiredOnly();
			case CHARGING -> percent < 0 ? new PowerState.Unknown() : new PowerState.Charging(percent);
			case CHARGED -> new PowerState.Full();
		};

		this.batteryLevelComponent.setBatteryLevel(level);
	}

	@Override
	public void update(ControllerImpl controller) {
		if (ptrController == null) {
			throw new IllegalStateException("Tried to update controller when it's closed.");
		}

		updateRumble();
		updateLED();
		updateDualSense();
	}

	protected void updateRumble() {
		if (isRumbleSupported) {
			Optional<RumbleState> stateOpt = this.rumbleComponent.consumeRumble();

			stateOpt.ifPresent(state -> {
				if (!SDL_RumbleController(ptrController, state.strong(), state.weak(), RUMBLE_TIMEOUT_MS)) {
					this.logger.error("Could not rumble gamepad: {}", sdl.error().SDL_GetError());
				}
			});
		}

		if (isTriggerRumbleSupported) {
			Optional<TriggerRumbleState> stateOpt = this.triggerRumbleComponent.consumeTriggerRumble();

			stateOpt.ifPresent(state -> {
				if (!SDL_RumbleControllerTriggers(ptrController, state.left(), state.right(), RUMBLE_TIMEOUT_MS)) {
					this.logger.error("Could not rumble triggers gamepad: {}", sdl.error().SDL_GetError());
				}
			});
		}
	}

	private void updateLED() {
		if (ledComponent == null) return;

		if (ledComponent.consumeDirty()) {
			int color = ledComponent.color();

			byte red = (byte) ((color >> 16) & 0xFF);
			byte green = (byte) ((color >> 8) & 0xFF);
			byte blue = (byte) (color & 0xFF);

			if (!SDL_SetControllerLED(ptrController, red, green, blue)) {
				logger.error("Could not set controller LED: {}", sdl.error().SDL_GetError());
			}
		}
	}

	private void updateDualSense() {
		if (dualSenseComponent == null) return;

		if (this.dualSenseComponent.consumeDirty()) {
			DualsenseEffectsState effectsState = new DualsenseEffectsState();

			// Left Trigger Effect
			Optional.ofNullable(this.dualSenseComponent.getLeftTriggerEffect()).ifPresent(effect -> {
				effectsState.ucEnableBits1 |= DualsenseEffectsState.EnableBitFlags1.ALLOW_LEFT_TRIGGER_FFB;
				effectsState.rgucLeftTriggerEffect = effect.createState();
			});

			// Right Trigger Effect
			Optional.ofNullable(this.dualSenseComponent.getRightTriggerEffect()).ifPresent(effect -> {
				effectsState.ucEnableBits1 |= DualsenseEffectsState.EnableBitFlags1.ALLOW_RIGHT_TRIGGER_FFB;
				effectsState.rgucRightTriggerEffect = effect.createState();
			});

			// Mute Light
			effectsState.ucEnableBits2 |= DualsenseEffectsState.EnableBitFlags2.ALLOW_MIC_LIGHT_MODE;
			effectsState.ucMicLightMode = DualsenseEffectsState.MicLightMode.fromEnum(this.dualSenseComponent.getMicLightMode());

			try (Arena arena = Arena.ofConfined()) {
				MemorySegment memory = arena.allocate(DualsenseEffectsState.LAYOUT);
				effectsState.writeTo(memory);
				SDL_SendControllerEffect(ptrController, memory.asByteBuffer());
			}
		}
	}

	@Override
	public void setActive(boolean active) {
		if (this.ptrController == null) {
			throw new IllegalStateException("Tried to activate or deactivate a closed controller.");
		}
		if (this.active == active) {
			return;
		}
		this.active = active;
		if (this.isGyroSupported && !SDL_SetControllerSensorEnabled(this.ptrController, SDL_SENSOR_GYRO, active)) {
			this.logger.warn("Failed to {} gyro sensor: {}", active ? "enable" : "disable", this.sdl.error().SDL_GetError());
		}
		if (this.isAccelSupported && !SDL_SetControllerSensorEnabled(this.ptrController, SDL_SENSOR_ACCEL, active)) {
			this.logger.warn("Failed to {} accel sensor: {}", active ? "enable" : "disable", this.sdl.error().SDL_GetError());
		}
	}

	@Override
	public void close() {
		if (ptrController == null) {
			throw new IllegalStateException("Tried to close controller when it's already closed.");
		}

		this.setActive(false);

		if (this.eventSubscription != null) {
			this.eventSubscription.close();
			this.eventSubscription = null;
		}

		if (dualSenseComponent != null) {
			dualSenseComponent.setLeftTriggerEffect(DualsenseTriggerEffect.Off.INSTANCE);
			dualSenseComponent.setRightTriggerEffect(DualsenseTriggerEffect.Off.INSTANCE);
			dualSenseComponent.setMicLightMode(MicLightMode.OFF);
			updateDualSense();
		}

		SDL_CloseController(ptrController);
		ptrController = null;

		if (hapticStream != null) {
			sdl.audio().SDL_DestroyAudioStream(hapticStream);
			hapticStream = null;
		}
		if (dualsenseAudioDev != null) {
			sdl.audio().SDL_CloseAudioDevice(dualsenseAudioDev);
			dualsenseAudioDev = null;
		}

		arena.close();
	}

	/// Whether this is the 2026 Steam Controller, wired, over Bluetooth or through its dongle.
	private boolean isSteamTriton() {
		return (this.vendorId & 0xFFFF) == 0x28DE && switch (this.productId & 0xFFFF) {
			case 0x1302, 0x1303, 0x1304, 0x1305 -> true;
			default -> false;
		};
	}

	private boolean isDualsense() {
		return (this.vendorId & 0xFFFF) == 0x54C && switch (this.productId & 0xFFFF) {
			case 0x0ce6, 0x0df2 -> true;
			default -> false;
		};
	}

	private boolean sendRawEffect(byte[] report) {
		if (ptrController == null) {
			return false;
		}
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment memory = arena.allocateFrom(ValueLayout.JAVA_BYTE, report);
			return SDL_SendControllerEffect(ptrController, memory.asByteBuffer());
		}
	}

	/// Opens the stream that haptics are synthesised into. The device has four channels:
	/// the first two are the speaker and headphone jack, the last two the haptic actuators.
	private void openHapticStream() {
		SdlAudioSpec streamSpec = new SdlAudioSpec(this.dualsenseAudioSpec.frequency(), SDL_AUDIO_F32, 2);
		SdlAudioStreamHandle stream = sdl.audio().SDL_CreateAudioStream(streamSpec, this.dualsenseAudioSpec);

		if (!sdl.audio().SDL_BindAudioStream(this.dualsenseAudioDev, stream)
				// leave the speaker channels silent and send left/right to the actuators
				|| !sdl.audio().SDL_SetAudioStreamOutputChannelMap(stream, new int[]{ -1, -1, 0, 1 })) {
			this.logger.warn("Could not open DualSense HD haptics stream: {}", sdl.error().SDL_GetError());
			sdl.audio().SDL_DestroyAudioStream(stream);
			return;
		}

		this.hapticStream = stream;
		this.hapticBuffer = this.arena.allocate((long) HAPTIC_BUFFER_FRAMES * 2 * Float.BYTES, Float.BYTES);
	}

	private final class HapticSink implements PcmSink {
		private static final int BYTES_PER_FRAME = 2 * Float.BYTES;

		@Override
		public int sampleRate() {
			return dualsenseAudioSpec.frequency();
		}

		@Override
		public int queuedFrames() {
			if (hapticStream == null) {
				return 0;
			}
			return Math.max(0, sdl.audio().SDL_GetAudioStreamQueued(hapticStream)) / BYTES_PER_FRAME;
		}

		@Override
		public void write(float[] samples, int frames) {
			int written = 0;
			while (written < frames && hapticStream != null) {
				int count = Math.min(frames - written, HAPTIC_BUFFER_FRAMES);
				MemorySegment.copy(samples, written * 2, hapticBuffer, ValueLayout.JAVA_FLOAT, 0, count * 2);
				ByteBuffer bytes = hapticBuffer.asSlice(0, (long) count * BYTES_PER_FRAME).asByteBuffer();
				if (!sdl.audio().SDL_PutAudioStreamData(hapticStream, bytes)) {
					return;
				}
				written += count;
			}
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
			bytes.add(this.guid.data().clone());
		}

		String uid = CUtil.createUIDFromBytes(bytes.toArray(new byte[0][]));

		// pick the first suffix not already in use, so removing a middle duplicate and
		// reconnecting cannot collide with a controller that is still connected
		Set<String> usedUids = Controlify.instance().getControllerManager().orElseThrow()
				.getAllControllers()
				.stream()
				.map(ControllerImpl::uid)
				.collect(Collectors.toSet());
		String candidate = uid;
		for (int suffix = 1; usedUids.contains(candidate); suffix++) {
			candidate = uid + "-" + suffix;
		}

		return candidate;
	}

	protected boolean isBluetooth() {
		return connectionState == SDLJoystickConnectionState.WIRELESS;
	}

	protected abstract SdlPropertiesId SDL_GetControllerProperties(SdlController ptrController);
	protected abstract String SDL_GetControllerName(SdlController ptrController);
	protected abstract SdlGuid SDL_GetControllerGUIDForID(SdlJoystickId jid);
	protected abstract @Nullable String SDL_GetControllerSerial(SdlController ptrController);
	protected abstract short SDL_GetControllerVendor(SdlController ptrController);
	protected abstract short SDL_GetControllerProduct(SdlController ptrController);
	//@MagicConstant(valuesFromClass = SDL_JoystickConnectionState.class)
	protected abstract int SDL_GetControllerConnectionState(SdlController ptrController);
	protected abstract boolean SDL_CloseController(SdlController ptrController);
	protected abstract boolean SDL_RumbleController(SdlController ptrController, float strong, float weak, int durationMs);
	protected abstract boolean SDL_RumbleControllerTriggers(SdlController ptrController, float left, float right, int durationMs);
	//@MagicConstant(valuesFromClass = SDL_PowerState.class)
	protected abstract int SDL_GetControllerPowerInfo(SdlController ptrController, SdlRefs.IntRef percent);
	protected abstract boolean SDL_SendControllerEffect(SdlController ptrController, ByteBuffer effect);
	protected abstract boolean SDL_SetControllerLED(SdlController ptrController, byte red, byte green, byte blue);
	protected abstract boolean SDL_ControllerHasSensor(SdlController ptrController, int type);
	protected abstract boolean SDL_SetControllerSensorEnabled(SdlController ptrController, int type, boolean enabled);
}
