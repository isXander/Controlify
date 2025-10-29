package dev.isxander.controlify.controllermanager;

import com.google.common.io.ByteStreams;
import com.sun.jna.Memory;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.info.ControllerInfo;
import dev.isxander.controlify.controller.id.ControllerType;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.CompoundDriver;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.driver.sdl.*;
import dev.isxander.controlify.driver.steamdeck.SteamDeckDriver;
import dev.isxander.controlify.driver.steamdeck.SteamDeckUtil;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.input.pipeline.EventBus;
import dev.isxander.controlify.input.pipeline.EventSource;
import dev.isxander.controlify.input.pipeline.SynchronousEventBus;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.ControllerUtils;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import dev.isxander.sdl3java.api.events.SdlEventTypes;
import dev.isxander.sdl3java.api.gamepad.SDL_Gamepad;
import dev.isxander.sdl3java.api.iostream.SDL_IOStream;
import dev.isxander.sdl3java.api.joystick.SDL_Joystick;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickGUID;
import dev.isxander.sdl3java.api.joystick.SDL_JoystickID;
import dev.isxander.sdl3java.jna.size_t;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static dev.isxander.sdl3java.api.error.SdlError.*;
import static dev.isxander.sdl3java.api.events.SDL_EventType.*;
import static dev.isxander.sdl3java.api.events.SdlEvents.*;
import static dev.isxander.sdl3java.api.gamepad.SdlGamepad.*;
import static dev.isxander.sdl3java.api.iostream.SdlIOStream.*;
import static dev.isxander.sdl3java.api.joystick.SdlJoystick.*;

public class SDLControllerManager extends AbstractControllerManager {

    private SdlEventTypes.SDL_Event event;
    private final EventBus<SdlEventTypes.SDL_Event> eventBus;
    private final EventSource<SdlControllerEvent> controllerEventSource;

    private boolean steamDeckConsumed = false;

    public SDLControllerManager(ControlifyLogger logger) {
        super(logger);
        logger.debugLog("Controller manager using SDL3");
        logger.validateIsTrue(SdlNativesLoader.isLoaded(), "SDL3 natives must be loaded before creating SDLControllerManager");
        this.event = new SdlEventTypes.SDL_Event();
        this.eventBus = SynchronousEventBus.createForFirstThread();
        this.controllerEventSource = this.eventBus.via(new SdlControllerEvent.AbstractionStage());
        this.controllerEventSource.subscribe(this::onSDLEvent);
    }

    // call this instead on RenderSystem#pollEvents to get frame-synced controller events
    @Override
    public void tick(boolean outOfFocus) {
        super.tick(outOfFocus);

        SDL_PumpEvents();

        if (event == null) {
            logger.error("SDL_Event has somehow been set to null. Recreating...");
            event = new SdlEventTypes.SDL_Event();
        }

        while (SDL_PollEvent(this.event)) {
            this.event.read(); // ensure the event structure is up to date
            // this bus must be synchronous
            this.eventBus.accept(this.event);
        }

        SDL_UpdateGamepads();
        SDL_UpdateJoysticks();
    }

    protected void onSDLEvent(SdlControllerEvent event) {
        switch (event) {
            case SdlControllerEvent.JoyDevice d when d.type() == SdlControllerEvent.JoyDevice.Type.ADDED -> {
                logger.debugLog("SDL event: Joystick added: {}", d.which());

                UniqueControllerID ucid = new SDLUniqueControllerID(d.which());

                Optional<ControllerEntity> controllerOpt = tryCreate(
                        ucid,
                        fetchTypeFromSDL(d.which())
                                .orElse(new ControllerHIDService.ControllerHIDInfo(ControllerType.DEFAULT, Optional.empty()))
                );
                controllerOpt.ifPresent(controller -> {
                    ControllerUtils.wrapControllerError(() -> onControllerConnected(controller, true), "Connecting controller", controller);
                });
            }

            case SdlControllerEvent.JoyDevice d when d.type() == SdlControllerEvent.DeviceEvent.Type.REMOVED -> {
                logger.debugLog("SDL event: Joystick removed: {}", d.which());

                UniqueControllerID ucid = new SDLUniqueControllerID(d.which());

                getController(ucid)
                        .ifPresentOrElse(
                                this::onControllerRemoved,
                                () -> logger.warn("Controller removed but not found: {}", d.which())
                        );
            }

            default -> {}
        }
    }

    @Override
    public void discoverControllers() {
        logger.debugLog("Discovering controllers...");

        SDL_JoystickID[] joysticks = SDL_GetJoysticks();
        for (SDL_JoystickID jid : joysticks) {
            Optional<ControllerEntity> controllerOpt = tryCreate(
                    new SDLUniqueControllerID(jid),
                    fetchTypeFromSDL(jid)
                            .orElse(new ControllerHIDService.ControllerHIDInfo(ControllerType.DEFAULT, Optional.empty()))
            );
            controllerOpt.ifPresent(controller -> onControllerConnected(controller, false));
        }
    }

    @Override
    protected Optional<ControllerEntity> createController(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo, ControlifyLogger controllerLogger) {
        SDL_JoystickID jid = ((SDLUniqueControllerID) ucid).jid();
        controllerLogger.debugLog("Creating controller: {}", jid.intValue());

        boolean isGamepad = isControllerGamepad(ucid) && !DebugProperties.FORCE_JOYSTICK;
        controllerLogger.debugLog("Controller is gamepad: {}", isGamepad);

        List<Driver> drivers = new ArrayList<>();
        if ((SteamDeckUtil.DECK_MODE.isGamingMode() || DebugProperties.STEAM_DECK_CUSTOM_CEF_URL != null)
            && !steamDeckConsumed
            && hidInfo.type().namespace().equals(SteamDeckUtil.STEAM_DECK_NAMESPACE)
        ) {
            controllerLogger.debugLog("Controller is steam deck candidate");
            Optional<SteamDeckDriver> steamDeckDriver = SteamDeckDriver.create(controllerLogger);
            if (steamDeckDriver.isPresent()) {
                drivers.add(steamDeckDriver.get());
                steamDeckConsumed = true;
                controllerLogger.debugLog("Adding SteamDeckDriver - this controller has been reserved for Steam Deck");
            }
        }

        // filter the event source
        EventSource<SdlControllerEvent> eventSource = this.controllerEventSource
                .filter(e -> e.which().equals(jid));

        if (isGamepad) {
            SDL_Gamepad ptrGamepad = SdlUtil.openGamepad(jid);
            drivers.add(new SdlGamepadDriver(ptrGamepad, jid, hidInfo.type(), eventSource, controllerLogger));
        } else {
            SDL_Joystick ptrJoystick = SdlUtil.openJoystick(jid);
            drivers.add(new SdlJoystickDriver(ptrJoystick, jid, hidInfo.type(), eventSource, controllerLogger));
        }

        controllerLogger.debugLog("Drivers: {}", drivers.stream().map(driver -> driver.getClass().getSimpleName()).collect(Collectors.joining(", ")));

        CompoundDriver compoundDriver = new CompoundDriver(drivers);

        ControllerInfo info = new ControllerInfo(ucid, hidInfo.type(), hidInfo.hidDevice());
        ControllerEntity controller = new ControllerEntity(info, compoundDriver, controllerLogger);

        controllerLogger.debugLog("Unique Controller ID: {}", info.ucid());

        this.addController(ucid, controller);
        return Optional.of(controller);
    }

    @Override
    public boolean probeConnectedControllers() {
        return SDL_HasJoystick() || SDL_HasGamepad();
    }

    @Override
    public boolean isControllerGamepad(UniqueControllerID ucid) {
        SDL_JoystickID jid = ((SDLUniqueControllerID) ucid).jid;
        return SDL_IsGamepad(jid);
    }

    @Override
    protected String getControllerSystemName(UniqueControllerID ucid) {
        SDL_JoystickID jid = ((SDLUniqueControllerID) ucid).jid;
        return isControllerGamepad(ucid) ? SDL_GetGamepadNameForID(jid) : SDL_GetJoystickNameForID(jid);
    }

    private Optional<ControllerEntity> getController(UniqueControllerID ucid) {
        return Optional.ofNullable(controllersByJid.getOrDefault(ucid, null));
    }

    @Override
    protected void loadGamepadMappings(ResourceProvider resourceProvider) {
        CUtil.LOGGER.debugLog("Loading gamepad mappings...");

        Optional<Resource> resourceOpt = resourceProvider
                .getResource(CUtil.rl("controllers/gamecontrollerdb-sdl3.txt"));
        if (resourceOpt.isEmpty()) {
            CUtil.LOGGER.error("Failed to find game controller database.");
            return;
        }

        try (InputStream is = resourceOpt.get().open()) {
            byte[] bytes = ByteStreams.toByteArray(is);

            try (Memory memory = new Memory(bytes.length)) {
                memory.write(0, bytes, 0, bytes.length);

                SDL_IOStream stream = SDL_IOFromConstMem(memory, new size_t(bytes.length));
                if (stream == null) throw new IllegalStateException("Failed to open stream");

                int count = SDL_AddGamepadMappingsFromIO(stream, true);
                if (count < 0) {
                    CUtil.LOGGER.error("Failed to load gamepad mappings: {}", SDL_GetError());
                } else if (count == 0) {
                    CUtil.LOGGER.warn("Successfully applied gamepad mappings but none were found for this OS. Unsupported OS?");
                } else {
                    CUtil.LOGGER.log("Successfully loaded {} gamepad mapping entries!", count);
                }
            }
        } catch (Throwable e) {
            CUtil.LOGGER.error("Failed to load gamepad mappings", e);
        }
    }

    private static Optional<ControllerHIDService.ControllerHIDInfo> fetchTypeFromSDL(SDL_JoystickID jid) {
        int vid = SDL_GetJoystickVendorForID(jid);
        int pid = SDL_GetJoystickProductForID(jid);
        SDL_JoystickGUID guid = SDL_GetJoystickGUIDForID(jid);
        String guidStr = guid.toString();

        if (vid != 0 && pid != 0) {
            CUtil.LOGGER.log("Using SDL to identify controller type.");
            return Optional.of(new ControllerHIDService.ControllerHIDInfo(
                    Controlify.instance().controllerTypeManager().getControllerType(new HIDIdentifier(vid, pid)),
                    Optional.of(new HIDDevice.SDLHidApi(vid, pid, guidStr))
            ));
        }

        return Optional.empty();
    }

    public record SDLUniqueControllerID(@NotNull SDL_JoystickID jid) implements UniqueControllerID {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof SDLUniqueControllerID && ((SDLUniqueControllerID) obj).jid.equals(jid);
        }

        @Override
        public String toString() {
            return "SDL-" + jid.longValue();
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid.longValue());
        }
    }
}
