package dev.isxander.controlify.impl.controllermanager;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.ControllerInfo;
import dev.isxander.controlify.impl.debug.DebugProperties;
import dev.isxander.controlify.impl.driver.Driver;
import dev.isxander.controlify.impl.driver.sdl.*;
import dev.isxander.controlify.impl.hid.HIDID;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.event.EventBus;
import dev.isxander.controlify.api.utils.event.EventSource;
import dev.isxander.controlify.impl.utils.log.ControlifyLogger;
import dev.isxander.sdl.*;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;

public class SDLControllerBackend extends ControllerBackend {
	private final Sdl sdl;
	private final ControlifyLogger logger;
	private final SDLControllerEventStage controllerEventStage;
	private final EventBus<SDLControllerEvent> controllerEventBus;

	private SdlEvent event;
	private long lastFrameTimeNanos = -1;

	public SDLControllerBackend(Sdl sdl, Controlify controlify, ControllerManager controllerManager) {
		super(controlify, controllerManager);
		this.sdl = sdl;
		this.logger = CUtil.LOGGER.createSubLogger("SDLControllerBackend");
		this.controllerEventStage = new SDLControllerEventStage();
		this.controllerEventBus = new EventBus<>();
		this.event = new SdlEvent();
	}

	@Override
	public void setup() {
		this.logger.debugLog("Loading gamepad mappings...");

		Optional<Resource> resourceOpt = Minecraft.getInstance().getResourceManager()
				.getResource(CUtil.rl("controller/gamecontrollerdb-sdl3.txt"));
		if (resourceOpt.isEmpty()) {
			this.logger.error("Failed to find game controller database.");
			return;
		}

		try (InputStream is = resourceOpt.get().open()) {
			byte[] bytes = is.readAllBytes();
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
			byteBuffer.put(bytes);
			byteBuffer.flip();

			SdlIoStreamHandle stream = null;
			try {
				stream = this.sdl.ioStream().SDL_IOFromConstMem(byteBuffer);
				if (stream.isNull()) {
					throw new IllegalStateException("Failed to open stream");
				}

				int count = this.sdl.gamepad().SDL_AddGamepadMappingsFromIO(stream, true);
				if (count < 0) {
					this.logger.error("Failed to load gamepad mappings: {}", this.sdl.error().SDL_GetError());
				} else if (count == 0) {
					this.logger.warn("Successfully applied gamepad mappings but none were applied. Unsupported OS?");
				} else {
					this.logger.log("Successfully loaded {} gamepad mapping entries!", count);
				}
			} finally {
				if (stream != null) {
					// I think SDL_AddGamepadMappingsFromIO closes this stream
					// as it JVM crashes if I call this
					//this.sdl.ioStream().SDL_CloseIO(stream);
				}
			}
		} catch (Throwable e) {
			this.logger.error("Failed to load gamepad mappings", e);
		}
	}

	@Override
	public void discoverControllers() {
		this.logger.debugLog("Discovering controllers...");

		SdlJoystickId[] joysticks = this.sdl.joystick().SDL_GetJoysticks();
		for (SdlJoystickId jid : joysticks) {
			this.tryCreateAndAddController(jid, false);
		}
	}

	@Override
	public void pollEvents() {
		if (this.event == null) {
			this.logger.warn("SDL_Event has somehow been set to null. Recreating...");
			this.event = new SdlEvent();
		}

		while (this.sdl.events().SDL_PollEvent(this.event)) {
			this.controllerEventStage.onEvent(this.event, this::onControllerEvent);
		}

		long timeNanos = this.sdl.timer().SDL_GetTicksNS();
		long deltaTimeNanos = this.lastFrameTimeNanos == -1 ? 0 : timeNanos - this.lastFrameTimeNanos;
		this.lastFrameTimeNanos = timeNanos;
		this.onControllerEvent(new SDLControllerEvent.Frame(timeNanos, deltaTimeNanos));
	}

	private void onControllerEvent(SDLControllerEvent event) {
		this.controllerEventBus.emit(event);

		switch (event) {
			case SDLControllerEvent.JoyDevice(SdlJoystickId which, _, SDLControllerEvent.DeviceEvent.Type eventType)
					when eventType == SDLControllerEvent.DeviceEvent.Type.ADDED -> {
				this.tryCreateAndAddController(which, true);
			}

			case SDLControllerEvent.JoyDevice(SdlJoystickId which, _, SDLControllerEvent.DeviceEvent.Type type)
					when type == SDLControllerEvent.DeviceEvent.Type.REMOVED -> {
				this.controllerManager.getControllerByUcid(new SDLUniqueControllerID(which))
						.ifPresent(controller -> this.controllerManager.removeController(controller.info().ucid()));
			}

			default -> {}
		}
	}

	private void tryCreateAndAddController(SdlJoystickId jid, boolean hotplugged) {
		HIDID hidId = this.getJoystickHIDID(jid);
		ControllerType type = this.getControllerType(hidId);
		this.createAndAddController(new ControllerInfo(new SDLUniqueControllerID(jid), type, hidId), hotplugged);
	}

	@Override
	public void createAndAddController(ControllerInfo info, boolean hotplugged) {
		if (!(info.ucid() instanceof SDLUniqueControllerID ucid)) {
			throw new IllegalArgumentException("SDL backend requires an SDL controller ID");
		}
		var logger = this.logger.createSubLogger("Controller #" + ucid);
		if (this.controllerManager.canAddController(ucid, info.hid(), info.type(), logger)) {
			this.createAndAddController(ucid, info.hid(), info.type(), logger, hotplugged);
		}
	}

	private void createAndAddController(
			SDLUniqueControllerID ucid,
			HIDID hidId,
			ControllerType type,
			ControlifyLogger controllerLogger,
			boolean hotplugged
	) {
		SdlJoystickId jid = ucid.jid();
		controllerLogger.debugLog("Creating controller: {}", jid.value());

		boolean isGamepad = this.sdl.gamepad().SDL_IsGamepad(jid) && !DebugProperties.FORCE_JOYSTICK;
		controllerLogger.debugLog("Controller is gamepad: {}", isGamepad);

		EventSource<SDLControllerEvent> controllerEventSource = this.controllerEventBus
				.filter(event -> event instanceof SDLControllerEvent.Frame
						|| event instanceof SDLControllerEvent.DeviceScopedEvent scoped && scoped.which().equals(jid));

		Driver driver;
		if (isGamepad) {
			SdlGamepadHandle gamepadHandle = SDLUtil.openGamepad(this.sdl, jid);
			driver = new SDL3GamepadDriver(this.sdl, gamepadHandle, controllerEventSource, jid, type, controllerLogger);
		} else {
			SdlJoystickHandle joystickHandle = SDLUtil.openJoystick(this.sdl, jid);
			driver = new SDL3JoystickDriver(this.sdl, joystickHandle, controllerEventSource, jid, type, controllerLogger);
		}

		var controllerInfo = new ControllerInfo(ucid, type, hidId);
		var controller = new ControllerImpl(
				controllerInfo,
				driver,
				controllerLogger,
				Controlify.instance().config().getActiveProfile(),
				ProfileSettings.createDefault()
		);

		this.controllerManager.addController(controller, hotplugged);
	}

	private HIDID getJoystickHIDID(SdlJoystickId jid) {
		int vid = this.sdl.joystick().SDL_GetJoystickVendorForID(jid);
		int pid = this.sdl.joystick().SDL_GetJoystickProductForID(jid);

		return new HIDID(vid, pid);
	}
}
