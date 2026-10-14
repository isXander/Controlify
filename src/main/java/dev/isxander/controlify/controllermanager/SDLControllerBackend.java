package dev.isxander.controlify.controllermanager;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.info.ControllerInfo;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.driver.sdl.*;
import dev.isxander.controlify.hid.HIDID;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.event.EventSource;
import dev.isxander.controlify.utils.event.EventTap;
import dev.isxander.controlify.utils.log.ControlifyLogger;
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
	private final EventTap<SDLControllerEvent> controllerEventTap;

	private SdlEvent event;

	public SDLControllerBackend(Sdl sdl, Controlify controlify, ControllerManager controllerManager) {
		super(controlify, controllerManager);
		this.sdl = sdl;
		this.logger = CUtil.LOGGER.createSubLogger("SDLControllerManagerBackend");
		this.controllerEventStage = new SDLControllerEventStage();
		this.controllerEventTap = new EventTap<>();
		this.event = new SdlEvent();
	}

	@Override
	public void setup() {
		this.logger.debugLog("Loading gamepad mappings...");

		Optional<Resource> resourceOpt = Minecraft.getInstance().getResourceManager()
				.getResource(CUtil.rl("controllers/gamecontrollerdb-sdl3.txt"));
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
				if (stream.isNull()) throw new IllegalStateException("Failed to open stream");

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
					this.sdl.ioStream().SDL_CloseIO(stream);
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
	}

	private void onControllerEvent(SDLControllerEvent event) {
		this.controllerEventTap.emit(event);

		switch (event) {
			case SDLControllerEvent.JoyDevice(SdlJoystickId which, _, SDLControllerEvent.DeviceEvent.Type eventType)
					when eventType == SDLControllerEvent.DeviceEvent.Type.ADDED -> {
				this.tryCreateAndAddController(which, true);
			}

			case SDLControllerEvent.JoyDevice(SdlJoystickId which, _, SDLControllerEvent.DeviceEvent.Type type)
					when type == SDLControllerEvent.DeviceEvent.Type.REMOVED -> {
				this.controllerManager.removeController(new SDLUniqueControllerID(which));
			}

			default -> {}
		}
	}

	private void tryCreateAndAddController(SdlJoystickId jid, boolean hotplugged) {
		var ucid = new SDLUniqueControllerID(jid);

		var logger = this.logger.createSubLogger("Controller #" + ucid);
		logger.debugLog("New controller detected!");

		HIDID hidId = this.getJoystickHIDID(jid);
		ControllerType type = this.getControllerType(hidId);

		if (this.controllerManager.canAddController(ucid, hidId, type, logger)) {
			this.createAndAddController(ucid, hidId, type, logger, hotplugged);
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

		EventSource<SDLControllerEvent> controllerEventSource = this.controllerEventTap
				.filter(event -> event.which().equals(jid));

		Driver driver;
		if (isGamepad) {
			SdlGamepadHandle gamepadHandle = SDLUtil.openGamepad(this.sdl, jid);
			driver = new SDL3GamepadDriver(this.sdl, gamepadHandle, controllerEventSource, jid, type, controllerLogger);
		} else {
			SdlJoystickHandle joystickHandle = SDLUtil.openJoystick(this.sdl, jid);
			driver = new SDL3JoystickDriver(this.sdl, joystickHandle, controllerEventSource, jid, type, controllerLogger);
		}

		var controllerInfo = new ControllerInfo(ucid, type, hidId);
		var controller = new ControllerEntity(
				controllerInfo,
				driver,
				Controlify.instance().config().getActiveProfile(),
				ProfileSettings.createDefault(),
				controllerLogger
		);

		this.controllerManager.addController(controller, hotplugged);
	}

	private HIDID getJoystickHIDID(SdlJoystickId jid) {
		int vid = this.sdl.joystick().SDL_GetJoystickVendorForID(jid);
		int pid = this.sdl.joystick().SDL_GetJoystickProductForID(jid);

		return new HIDID(vid, pid);
	}
}
