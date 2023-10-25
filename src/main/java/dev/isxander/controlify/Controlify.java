package dev.isxander.controlify;

import com.google.common.io.ByteStreams;
import com.mojang.blaze3d.Blaze3D;
import com.sun.jna.Memory;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.compatibility.ControlifyCompat;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;
import dev.isxander.controlify.gui.controllers.ControllerBindHandler;
import dev.isxander.controlify.gui.screen.*;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.driver.SDL2NativesManager;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.ingame.ControllerPlayerMovement;
import dev.isxander.controlify.server.*;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.config.ControlifyConfig;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.gui.guide.InGameButtonGuide;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.isxander.controlify.mixins.feature.virtualmouse.MouseHandlerAccessor;
import dev.isxander.controlify.utils.*;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import dev.isxander.controlify.wireless.LowBatteryNotifier;
import io.github.libsdl4j.api.rwops.SDL_RWops;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerAddMappingsFromRW;
import static io.github.libsdl4j.api.rwops.SdlRWops.SDL_RWFromConstMem;

public class Controlify implements ControlifyApi {
    private static Controlify instance = null;

    private final Minecraft minecraft = Minecraft.getInstance();

    private Controller<?, ?> currentController = null;
    private InGameInputHandler inGameInputHandler;
    public InGameButtonGuide inGameButtonGuide;
    private VirtualMouseHandler virtualMouseHandler;
    private InputMode currentInputMode = InputMode.KEYBOARD_MOUSE;
    private ControllerHIDService controllerHIDService;

    private CompletableFuture<Boolean> nativeOnboardingFuture = null;

    private final ControlifyConfig config = new ControlifyConfig(this);

    private final Queue<Controller<?, ?>> calibrationQueue = new ArrayDeque<>();
    private boolean hasDiscoveredControllers = false;

    private int consecutiveInputSwitches = 0;
    private double lastInputSwitchTime = 0;

    private int showMouseTicks = 0;

    private @Nullable Controller<?, ?> switchableController = null;
    private double askSwitchTime = 0;
    private ToastUtils.ControlifyToast askSwitchToast = null;

    /**
     * Called at usual fabric client entrypoint.
     * Always runs, even with no controllers detected.
     * In this state, Controlify is only partially loaded, no controllers
     * have been initialised, nor has the config. This is done at {@link Controlify#initializeControlify()}.
     * This is where regular fabric callbacks are registered.
     */
    public void preInitialiseControlify() {
        DebugProperties.printProperties();

        Log.LOGGER.info("Pre-initializing Controlify...");

        this.inGameInputHandler = null; // set when the current controller changes
        this.virtualMouseHandler = new VirtualMouseHandler();

        controllerHIDService = new ControllerHIDService();
        controllerHIDService.start();

        ControllerBindHandler.setup();

        ClientPlayNetworking.registerGlobalReceiver(VibrationPacket.TYPE, (packet, player, sender) -> {
            if (config().globalSettings().allowServerRumble) {
                getCurrentController().ifPresent(controller ->
                        controller.rumbleManager().play(packet.source(), packet.createEffect()));
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(OriginVibrationPacket.TYPE, (packet, player, sender) -> {
            if (config().globalSettings().allowServerRumble) {
                getCurrentController().ifPresent(controller ->
                        controller.rumbleManager().play(packet.source(), packet.createEffect()));
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(EntityVibrationPacket.TYPE, (packet, player, sender) -> {
            if (config().globalSettings().allowServerRumble) {
                getCurrentController().ifPresent(controller ->
                        controller.rumbleManager().play(packet.source(), packet.createEffect()));
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(ServerPolicyPacket.TYPE, (packet, player, sender) -> {
            Log.LOGGER.info("Connected server specified '{}' policy is {}.", packet.id(), packet.allowed() ? "ALLOWED" : "DISALLOWED");
            ServerPolicies.getById(packet.id()).set(ServerPolicy.fromBoolean(packet.allowed()));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DebugLog.log("Disconnected from server, resetting server policies");
            ServerPolicies.unsetAll();
        });

        FabricLoader.getInstance().getEntrypoints("controlify", ControlifyEntrypoint.class).forEach(entrypoint -> {
            try {
                entrypoint.onControlifyPreInit(this);
            } catch (Exception e) {
                Log.LOGGER.error("Failed to run `onControlifyPreInit` on Controlify entrypoint: " + entrypoint.getClass().getName(), e);
            }
        });
    }

    /**
     * Called once Minecraft has completely loaded.
     * (When the loading overlay starts to fade).
     *
     * This is where controllers are usually initialised, as long
     * as one or more controllers are connected.
     */
    public void initializeControlify() {
        Log.LOGGER.info("Initializing Controlify...");

        config().load();

        // initialise and compatability modules that controlify implements itself
        // this does NOT invoke any entrypoints. this is done in the pre-initialisation phase
        ControlifyCompat.init();

        var controllersConnected = IntStream.range(0, GLFW.GLFW_JOYSTICK_LAST + 1)
                .anyMatch(GLFW::glfwJoystickPresent);
        if (controllersConnected) { // only initialise Controlify if controllers are detected
            if (!config().globalSettings().delegateSetup) {
                // check native onboarding then discover controllers
                askNatives().whenComplete((loaded, th) -> discoverControllers());
            } else {
                // delegate setup: don't auto set up controllers, require the user to open config screen
                ToastUtils.sendToast(
                        Component.translatable("controlify.toast.setup_in_config.title"),
                        Component.translatable(
                                "controlify.toast.setup_in_config.description",
                                Component.translatable("options.title"),
                                Component.translatable("controls.keybinds.title"),
                                Component.literal("Controlify")
                        ),
                        false
                );
            }
        }

        // register events
        ClientTickEvents.START_CLIENT_TICK.register(this::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraft -> {
            controllerHIDService().stop();
        });
        ConnectServerEvent.EVENT.register((minecraft, address, data) -> {
            notifyNewServer(data);
        });

        // set up the hotplugging callback with GLFW
        // TODO: investigate if there is any benefit to implementing this with SDL
        GLFW.glfwSetJoystickCallback((jid, event) -> {
            try {
                this.askNatives().whenComplete((loaded, th) -> {
                    if (event == GLFW.GLFW_CONNECTED) {
                        this.onControllerHotplugged(jid);
                    } else if (event == GLFW.GLFW_DISCONNECTED) {
                        this.onControllerDisconnect(jid);
                    }
                });
            } catch (Throwable e) {
                Log.LOGGER.error("Failed to handle controller connect/disconnect event", e);
            }
        });

        // sends toasts of new features
        notifyOfNewFeatures();
    }

    /**
     * Loops through every controller slot and initialises it if it is connected.
     * This is guaranteed to be called at most once. If no controllers are connected
     * in the whole game lifecycle, this is never ran.
     */
    public void discoverControllers() {
        if (hasDiscoveredControllers) {
            Log.LOGGER.warn("Attempted to discover controllers twice!");
        }
        hasDiscoveredControllers = true;

        DebugLog.log("Discovering and initializing controllers...");

        // load gamepad mappings before every
        minecraft.getResourceManager()
                .getResource(Controlify.id("controllers/gamecontrollerdb.txt"))
                .ifPresent(this::loadGamepadMappings);

        // find already connected controllers
        // TODO: investigate if there is any benefit to implementing this with SDL
        for (int jid = 0; jid <= GLFW.GLFW_JOYSTICK_LAST; jid++) {
            if (GLFW.glfwJoystickPresent(jid)) {
                Optional<Controller<?, ?>> controllerOpt = ControllerManager.createOrGet(
                        jid,
                        controllerHIDService.fetchType(jid)
                );
                if (controllerOpt.isEmpty())
                    continue;
                Controller<?, ?> controller = controllerOpt.get();

                Log.LOGGER.info("Controller found: " + ControllerUtils.createControllerString(controller));

                boolean newController = !config().loadOrCreateControllerData(controller);

                if (SubmitUnknownControllerScreen.canSubmit(controller)) {
                    minecraft.setScreen(new SubmitUnknownControllerScreen(controller, minecraft.screen));
                }

                // only "equip" the controller if it has already been calibrated
                if (!controller.config().deadzonesCalibrated) {
                    calibrationQueue.add(controller);
                } else if (controller.uid().equals(config().currentControllerUid())) {
                    setCurrentController(controller);
                }

                // make sure that allow vibrations is not mismatched with the native library setting
                if (controller.config().allowVibrations && !config().globalSettings().loadVibrationNatives) {
                    controller.config().allowVibrations = false;
                    config().setDirty();
                }

                // if a joystick and unmapped, tell the user that they need to configure the controls
                // joysticks have an abstract number of inputs, so applying a default control scheme is impossible
                if (newController && controller instanceof JoystickController<?> joystick && joystick.mapping() instanceof UnmappedJoystickMapping) {
                    ToastUtils.sendToast(
                            Component.translatable("controlify.toast.unmapped_joystick.title"),
                            Component.translatable("controlify.toast.unmapped_joystick.description", controller.name()),
                            true
                    );
                }
            }
        }

        if (ControllerManager.getConnectedControllers().isEmpty()) {
            Log.LOGGER.info("No controllers found.");
        }

        // if no controller is currently selected, select the first one
        if (getCurrentController().isEmpty()) {
            var controller = ControllerManager.getConnectedControllers().stream().findFirst().orElse(null);
            if (controller != null && (controller.config().delayedCalibration || !controller.config().deadzonesCalibrated)) {
                controller = null;
            }

            this.setCurrentController(controller);
        } else {
            // setCurrentController saves config so there is no need to set dirty to save
            config().saveIfDirty();
        }

        FabricLoader.getInstance().getEntrypoints("controlify", ControlifyEntrypoint.class).forEach(entrypoint -> {
            try {
                entrypoint.onControllersDiscovered(this);
            } catch (Throwable e) {
                Log.LOGGER.error("Failed to run `onControllersDiscovered` on Controlify entrypoint: " + entrypoint.getClass().getName(), e);
            }
        });
    }

    /**
     * Called when a controller has been connected after mod initialisation.
     * If this is the first controller to be connected in the game's lifecycle,
     * this is delegated to {@link Controlify#discoverControllers()} for it to be "discovered",
     * otherwise the controller is initialised and added to the list of connected controllers.
     */
    private void onControllerHotplugged(int jid) {
        if (!hasDiscoveredControllers) {
            discoverControllers();
            return;
        }

        var controllerOpt = ControllerManager.createOrGet(jid, controllerHIDService.fetchType(jid));
        if (controllerOpt.isEmpty()) return;
        var controller = controllerOpt.get();

        Log.LOGGER.info("Controller connected: " + ControllerUtils.createControllerString(controller));

        config().loadOrCreateControllerData(controller);

        if (SubmitUnknownControllerScreen.canSubmit(controller)) {
            minecraft.setScreen(new SubmitUnknownControllerScreen(controller, minecraft.screen));
        }

        if (config().globalSettings().delegateSetup) {
            config().globalSettings().delegateSetup = false;
            config().setDirty();
        }

        if (controller.config().allowVibrations && !config().globalSettings().loadVibrationNatives) {
            controller.config().allowVibrations = false;
            config().setDirty();
        }

        if (ControllerManager.getConnectedControllers().size() == 1 && (controller.config().deadzonesCalibrated || controller.config().delayedCalibration)) {
            this.setCurrentController(controller);

            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.default_controller_connected.title"),
                    Component.translatable("controlify.toast.default_controller_connected.description"),
                    false
            );
        } else {
            this.askToSwitchController(controller);
            config().saveIfDirty();
        }

        if (minecraft.screen instanceof ControllerCarouselScreen controllerListScreen) {
            controllerListScreen.refreshControllers();
        }
    }

    /**
     * Called when a controller is disconnected.
     * Equips another controller if available.
     *
     * @param jid the joystick id of the disconnected controller
     */
    private void onControllerDisconnect(int jid) {
        ControllerManager.getConnectedControllers().stream().filter(controller -> controller.joystickId() == jid).findAny().ifPresent(controller -> {
            ControllerManager.disconnect(controller);

            controller.hidInfo().ifPresent(controllerHIDService::unconsumeController);

            setCurrentController(ControllerManager.getConnectedControllers().stream().findFirst().orElse(null));
            Log.LOGGER.info("Controller disconnected: " + controller.name());
            this.setInputMode(currentController == null ? InputMode.KEYBOARD_MOUSE : InputMode.CONTROLLER);

            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.controller_disconnected.title"),
                    Component.translatable("controlify.toast.controller_disconnected.description", controller.name()),
                    false
            );

            if (minecraft.screen instanceof ControllerCarouselScreen controllerListScreen) {
                controllerListScreen.refreshControllers();
            }
        });
    }

    /**
     * Asks the user if they want to download the SDL2 library,
     * or initialises it if it hasn't been already.
     * If the user has already been asked and SDL is already initialised,
     * a completed future is returned.
     * The future is completed once the user has made their choice and SDL
     * has been downloaded and initialised (or not).
     */
    public CompletableFuture<Boolean> askNatives() {
        // if the future already exists, just return it
        if (nativeOnboardingFuture != null)
            return nativeOnboardingFuture;

        // the user has already been asked, initialise SDL if necessary
        // and return a completed future
        if (config().globalSettings().vibrationOnboarded) {
            if (config().globalSettings().loadVibrationNatives) {
                return nativeOnboardingFuture = SDL2NativesManager.maybeLoad();
            }
            // micro-optimization. no need to create a new future every time. use the first not null check
            return nativeOnboardingFuture = CompletableFuture.completedFuture(false);
        }

        nativeOnboardingFuture = new CompletableFuture<>();

        // open the SDL onboarding screen. complete the future when the user has made their choice
        Screen parent = minecraft.screen;
        minecraft.setScreen(new SDLOnboardingScreen(
                () -> parent,
                answer -> {
                    if (answer) {
                        SDL2NativesManager.maybeLoad().whenComplete((loaded, th) -> {
                            if (th != null) nativeOnboardingFuture.completeExceptionally(th);
                            else nativeOnboardingFuture.complete(loaded);
                        });
                    } else {
                        nativeOnboardingFuture.complete(false);
                    }
                }
        ));

        return nativeOnboardingFuture;
    }

    /**
     * Loads the gamepad mappings for both GLFW and SDL2.
     * @param resource the already located `gamecontrollerdb.txt` resource
     */
    private void loadGamepadMappings(Resource resource) {
        try (InputStream is = resource.open()) {
            byte[] bytes = ByteStreams.toByteArray(is);

            ByteBuffer buffer = MemoryUtil.memASCIISafe(new String(bytes));
            if (!GLFW.glfwUpdateGamepadMappings(buffer)) {
                Log.LOGGER.error("GLFW failed to load gamepad mappings!");
            }

            if (SDL2NativesManager.isLoaded()) {
                try (Memory memory = new Memory(bytes.length)) {
                    memory.write(0, bytes, 0, bytes.length);
                    SDL_RWops rw = SDL_RWFromConstMem(memory, (int) memory.size());
                    int count = SDL_GameControllerAddMappingsFromRW(rw, 1);
                    if (count < 1) {
                        Log.LOGGER.error("SDL2 failed to load gamepad mappings!");
                    }
                }
            }
        } catch (Throwable e) {
            Log.LOGGER.error("Failed to load gamecontrollerdb.txt", e);
        }
    }

    /**
     * The main loop of Controlify.
     * In Controlify's current state, only the current controller is ticked.
     */
    public void tick(Minecraft client) {
        if (minecraft.getOverlay() == null) {
            if (!calibrationQueue.isEmpty() && !(minecraft.screen instanceof DontInteruptScreen)) {
                Screen screen = minecraft.screen;
                while (!calibrationQueue.isEmpty()) {
                    screen = new ControllerCalibrationScreen(calibrationQueue.poll(), screen);
                }
                minecraft.setScreen(screen);
            }
        }

        boolean outOfFocus = !config().globalSettings().outOfFocusInput && !client.isWindowActive();

        for (var controller : ControllerManager.getConnectedControllers()) {
            if (!outOfFocus)
                wrapControllerError(controller::updateState, "Updating controller state", controller);
            else
                wrapControllerError(controller::clearState, "Clearing controller state", controller);
            ControlifyEvents.CONTROLLER_STATE_UPDATE.invoker().onControllerStateUpdate(controller);
        }

        if (switchableController != null && Blaze3D.getTime() - askSwitchTime <= 10000) {
            if (switchableController.state().hasAnyInput()) {
                switchableController.clearState();
                this.setCurrentController(switchableController); // setCurrentController sets switchableController to null
                if (askSwitchToast != null) {
                    askSwitchToast.remove();
                    askSwitchToast = null;
                }
            }
        }

        if (minecraft.mouseHandler.isMouseGrabbed())
            showMouseTicks = 0;
        if (currentInputMode() == InputMode.MIXED && showMouseTicks > 0) {
            showMouseTicks--;
            if (showMouseTicks == 0) {
                hideMouse(true, false);
                if (virtualMouseHandler().requiresVirtualMouse()) {
                    virtualMouseHandler().enableVirtualMouse();
                }
            }
        }

        LowBatteryNotifier.tick();

        getCurrentController().ifPresent(currentController -> {
            wrapControllerError(
                    () -> tickController(currentController, outOfFocus),
                    "Ticking current controller",
                    currentController
            );
        });
    }

    private void tickController(Controller<?, ?> controller, boolean outOfFocus) {
        ControllerState state = controller.state();

        if (outOfFocus) {
            state = ControllerState.EMPTY;
            controller.rumbleManager().setSilent(true);
        } else {
            controller.rumbleManager().setSilent(false);
            controller.rumbleManager().tick();
        }

        if (state.hasAnyInput()) {
            this.setInputMode(controller.config().mixedInput ? InputMode.MIXED : InputMode.CONTROLLER);
        }

        if (consecutiveInputSwitches > 100) {
            Log.LOGGER.warn("Controlify detected current controller to be constantly giving input and has been disabled.");
            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.faulty_input.title"),
                    Component.translatable("controlify.toast.faulty_input.description"),
                    true
            );
            this.setCurrentController(null);
            consecutiveInputSwitches = 0;
            return;
        }

        this.virtualMouseHandler().handleControllerInput(controller);
        if (minecraft.screen != null) {
            ScreenProcessorProvider.provide(minecraft.screen).onControllerUpdate(controller);
        }
        if (minecraft.level != null) {
            this.inGameInputHandler().ifPresent(InGameInputHandler::inputTick);
        }

        ControlifyEvents.ACTIVE_CONTROLLER_TICKED.invoker().onControllerStateUpdate(controller);
    }

    public static void wrapControllerError(Runnable runnable, String errorTitle, Controller<?, ?> controller) {
        try {
            runnable.run();
        } catch (Throwable e) {
            CrashReport crashReport = CrashReport.forThrowable(e, errorTitle);
            CrashReportCategory category = crashReport.addCategory("Affected controller");
            category.setDetail("Controller name", controller.name());
            category.setDetail("Controller identification", controller.type().toString());
            category.setDetail("Controller type", controller.getClass().getCanonicalName());
            throw new ReportedException(crashReport);
        }
    }

    public ControlifyConfig config() {
        return config;
    }

    private void askToSwitchController(Controller<?, ?> controller) {
        this.switchableController = controller;
        this.askSwitchTime = Blaze3D.getTime();

        askSwitchToast = ToastUtils.sendToast(
                Component.translatable("controlify.toast.ask_to_switch.title"),
                Component.translatable("controlify.toast.ask_to_switch.description", controller.name()),
                true
        );
    }

    @Override
    @Deprecated
    public @NotNull Controller<?, ?> currentController() {
        if (currentController == null)
            return Controller.DUMMY;

        return currentController;
    }

    @Override
    public @NotNull Optional<Controller<?, ?>> getCurrentController() {
        return Optional.ofNullable(currentController);
    }

    public void setCurrentController(@Nullable Controller<?, ?> controller) {
        if (this.currentController == controller) return;

        this.currentController = controller;

        if (switchableController == controller) {
            switchableController = null;
        }

        if (controller == null) {
            this.setInputMode(InputMode.KEYBOARD_MOUSE);
            this.inGameInputHandler = null;
            this.inGameButtonGuide = null;
            DebugLog.log("Updated current controller to null");
            config().save();
            return;
        }

        DebugLog.log("Updated current controller to {}({})", controller.name(), controller.uid());

        if (!controller.uid().equals(config().currentControllerUid())) {
            config().save();
        }

        this.inGameInputHandler = new InGameInputHandler(controller);

        setInputMode(controller.config().mixedInput ? InputMode.MIXED : InputMode.CONTROLLER);

        if (!controller.config().deadzonesCalibrated)
            calibrationQueue.add(controller);
    }

    public Optional<InGameInputHandler> inGameInputHandler() {
        return Optional.ofNullable(inGameInputHandler);
    }

    public Optional<InGameButtonGuide> inGameButtonGuide() {
        return Optional.ofNullable(inGameButtonGuide);
    }

    public VirtualMouseHandler virtualMouseHandler() {
        return virtualMouseHandler;
    }

    public ControllerHIDService controllerHIDService() {
        return controllerHIDService;
    }

    public @NotNull InputMode currentInputMode() {
        return currentInputMode;
    }

    @Override
    public boolean setInputMode(@NotNull InputMode currentInputMode) {
        if (this.currentInputMode == currentInputMode) return false;
        this.currentInputMode = currentInputMode;

        if (!minecraft.mouseHandler.isMouseGrabbed())
            hideMouse(currentInputMode.isController(), true);
        if (minecraft.screen != null) {
            ScreenProcessorProvider.provide(minecraft.screen).onInputModeChanged(currentInputMode);
        }
        if (Minecraft.getInstance().player != null) {
            if (currentInputMode == InputMode.KEYBOARD_MOUSE) {
                this.inGameButtonGuide = null;
            } else {
                this.inGameButtonGuide = this.getCurrentController().map(c -> new InGameButtonGuide(c, Minecraft.getInstance().player)).orElse(null);
            }
        }
        if (Blaze3D.getTime() - lastInputSwitchTime < 20) {
            consecutiveInputSwitches++;
        } else {
            consecutiveInputSwitches = 0;
        }
        lastInputSwitchTime = Blaze3D.getTime();

        if (this.currentInputMode.isController()) {
            getCurrentController().ifPresent(Controller::clearState);
            if (minecraft.getCurrentServer() != null) {
                notifyNewServer(minecraft.getCurrentServer());
            }
        }

        ControllerPlayerMovement.updatePlayerInput(minecraft.player);

        ControlifyEvents.INPUT_MODE_CHANGED.invoker().onInputModeChanged(currentInputMode);

        return true;
    }

    public void hideMouse(boolean hide, boolean moveMouse) {
        GLFW.glfwSetInputMode(
                minecraft.getWindow().getWindow(),
                GLFW.GLFW_CURSOR,
                hide
                        ? GLFW.GLFW_CURSOR_HIDDEN
                        : GLFW.GLFW_CURSOR_NORMAL
        );
        if (minecraft.screen != null) {
            var mouseHandlerAccessor = (MouseHandlerAccessor) minecraft.mouseHandler;
            if (hide && !virtualMouseHandler().isVirtualMouseEnabled() && moveMouse) {
                // stop mouse hovering over last element before hiding cursor but don't actually move it
                // so when the user switches back to mouse it will be in the same place
                mouseHandlerAccessor.invokeOnMove(minecraft.getWindow().getWindow(), -50, -50);
            }
        }
    }

    public void showCursorTemporarily() {
        if (currentInputMode() == InputMode.MIXED && !minecraft.mouseHandler.isMouseGrabbed()) {
            hideMouse(false, false);
            showMouseTicks = 20 * 2;
            if (virtualMouseHandler().isVirtualMouseEnabled()) {
                virtualMouseHandler().disableVirtualMouse();
            }
        }
    }

    private void notifyOfNewFeatures() {
        if (config().isFirstLaunch())
            return;

        var newFeatureVersions = List.of(
                "1.5.0"
        ).iterator();

        String foundVersion = null;
        while (foundVersion == null && newFeatureVersions.hasNext()) {
            var version = newFeatureVersions.next();
            if (config().isLastSeenVersionLessThan(version)) {
                foundVersion = version;
            }
        }

        if (foundVersion != null) {
            Log.LOGGER.info("Sending new features toast for {}", foundVersion);
            ToastUtils.sendToast(
                    Component.translatable("controlify.new_features.title", foundVersion),
                    Component.translatable("controlify.new_features." + foundVersion),
                    true
            );
        }
    }

    private void notifyNewServer(ServerData data) {
        if (!currentInputMode().isController())
            return;

        if (config().globalSettings().seenServers.add(data.ip)) {
            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.new_server.title"),
                    Component.translatable("controlify.toast.new_server.description", data.name),
                    true
            );
            config().save();
        }
    }

    public static Controlify instance() {
        if (instance == null) instance = new Controlify();
        return instance;
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation("controlify", path);
    }
}
