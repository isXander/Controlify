package dev.isxander.controlify;

import com.mojang.blaze3d.Blaze3D;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.compatibility.ControlifyCompat;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.controllermanager.GLFWControllerManager;
import dev.isxander.controlify.controllermanager.SDLControllerManager;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import static dev.isxander.controlify.utils.ControllerUtils.wrapControllerError;

public class Controlify implements ControlifyApi {
    private static Controlify instance = null;

    private final Minecraft minecraft = Minecraft.getInstance();

    private ControllerManager controllerManager;

    private boolean finishedInit = false;
    private boolean probeMode = false;

    private Controller<?, ?> currentController = null;
    private InputMode currentInputMode = InputMode.KEYBOARD_MOUSE;

    private InGameInputHandler inGameInputHandler;
    public InGameButtonGuide inGameButtonGuide;
    private VirtualMouseHandler virtualMouseHandler;

    private ControllerHIDService controllerHIDService;

    private CompletableFuture<Boolean> nativeOnboardingFuture = null;

    private final ControlifyConfig config = new ControlifyConfig(this);

    private final Queue<Controller<?, ?>> calibrationQueue = new ArrayDeque<>();
    private boolean hasDiscoveredControllers = false;

    private int consecutiveInputSwitches = 0;
    private double lastInputSwitchTime = 0;

    private int showMouseTicks = 0;

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

        boolean controllersConnected = GLFWControllerManager.areControllersConnected();

        ControlifyEvents.CONTROLLER_CONNECTED.register(this::onControllerAdded);
        ControlifyEvents.CONTROLLER_DISCONNECTED.register(this::onControllerRemoved);

        if (controllersConnected) {
            if (config().globalSettings().quietMode) {
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
            } else {
                finishControlifyInit();
            }
        } else {
            probeMode = true;
            ClientTickEvents.END_CLIENT_TICK.register(client -> this.probeTick());
        }

        // register events
        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraft -> {
            controllerHIDService().stop();
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
            return;
        }
        hasDiscoveredControllers = true;

        DebugLog.log("Discovering and initializing controllers...");

        controllerManager.discoverControllers();

        if (controllerManager.getConnectedControllers().isEmpty()) {
            Log.LOGGER.info("No controllers found.");
        }

        // if no controller is currently selected, pick one
        if (getCurrentController().isEmpty()) {
            Optional<Controller<?, ?>> lastUsedController = controllerManager.getConnectedControllers()
                    .stream()
                    .filter(c -> c.uid().equals(config().currentControllerUid()))
                    .findAny();

            if (lastUsedController.isPresent()) {
                this.setCurrentController(lastUsedController.get(), false);
            } else {
                Controller<?, ?> anyController = controllerManager.getConnectedControllers()
                        .stream()
                        .filter(c -> !c.config().delayedCalibration && c.config().deadzonesCalibrated)
                        .findFirst()
                        .orElse(null);

                this.setCurrentController(anyController, false);
            }
        }

        config().saveIfDirty();

        FabricLoader.getInstance().getEntrypoints("controlify", ControlifyEntrypoint.class).forEach(entrypoint -> {
            try {
                entrypoint.onControllersDiscovered(this);
            } catch (Throwable e) {
                Log.LOGGER.error("Failed to run `onControllersDiscovered` on Controlify entrypoint: " + entrypoint.getClass().getName(), e);
            }
        });
    }

    /**
     * Completely finishes controlify initialization.
     * This can be run at any point during the game's lifecycle.
     * @return the future that completes when controlify has finished initializing
     */
    public CompletableFuture<Void> finishControlifyInit() {
        if (finishedInit) {
            return CompletableFuture.completedFuture(null);
        }
        probeMode = false;
        finishedInit = true;

        askNatives().whenComplete((loaded, th) -> {
            Log.LOGGER.info("Finishing Controlify init...");

            controllerManager = loaded ? new SDLControllerManager() : new GLFWControllerManager();

            ClientTickEvents.START_CLIENT_TICK.register(this::tick);
            ConnectServerEvent.EVENT.register((minecraft, address, data) -> {
                notifyNewServer(data);
            });

            // initialise and compatability modules that controlify implements itself
            // this does NOT invoke any entrypoints. this is done in the pre-initialisation phase
            ControlifyCompat.init();

            // make sure people don't someone add binds after controllers could have been created
            ControllerBindings.lockRegistry();

            if (config().globalSettings().quietMode) {
                config().globalSettings().quietMode = false;
                config().setDirty();
            }

            discoverControllers();
        });

        return askNatives().thenApply(loaded -> null);
    }

    /**
     * Called when a controller is connected. Either from controller
     * discovery or hotplugging.
     *
     * @param controller the new controller
     * @param hotplugged if this was a result of hotplugging
     * @param newController if this controller has never been seen before
     */
    private void onControllerAdded(Controller<?, ?> controller, boolean hotplugged, boolean newController) {
        if (SubmitUnknownControllerScreen.canSubmit(controller)) {
            minecraft.setScreen(new SubmitUnknownControllerScreen(controller, minecraft.screen));
        }

        if (controller.config().allowVibrations && !SDL2NativesManager.isLoaded()) {
            controller.config().allowVibrations = false;
            config().setDirty();
        }

        if (!controller.config().deadzonesCalibrated) {
            calibrationQueue.add(controller);
        } else if (hotplugged) {
            setCurrentController(controller, true);
        }

        if (controller instanceof JoystickController<?> joystick && joystick.mapping() instanceof UnmappedJoystickMapping) {
            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.unmapped_joystick.title"),
                    Component.translatable("controlify.toast.unmapped_joystick.description", controller.name()),
                    true
            );
        } else if (hotplugged) {
            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.controller_connected.title"),
                    Component.translatable("controlify.toast.controller_connected.description", controller.name()),
                    false
            );
        }

        if (minecraft.screen instanceof ControllerCarouselScreen controllerListScreen) {
            controllerListScreen.refreshControllers();
        }

        // saved after discovery
        if (hotplugged) {
            config().saveIfDirty();
        }
    }

    /**
     * Called when a controller is disconnected.
     * @param controller controller that has been disconnected
     */
    private void onControllerRemoved(Controller<?, ?> controller) {
        this.setCurrentController(
                controllerManager.getConnectedControllers()
                        .stream()
                        .findFirst()
                        .orElse(null),
                true);

        this.setInputMode(
                getCurrentController().isEmpty()
                        ? InputMode.KEYBOARD_MOUSE
                        : InputMode.CONTROLLER
        );

        ToastUtils.sendToast(
                Component.translatable("controlify.toast.controller_disconnected.title"),
                Component.translatable("controlify.toast.controller_disconnected.description", controller.name()),
                false
        );
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

        // just say no if the platform doesn't support it
        if (!SDL2NativesManager.isSupportedOnThisPlatform()) {
            Log.LOGGER.warn("SDL is not supported on this platform. Platform: {}", SDL2NativesManager.Target.CURRENT);
            nativeOnboardingFuture = new CompletableFuture<>();
            minecraft.setScreen(new NoSDLScreen(() -> nativeOnboardingFuture.complete(false), minecraft.screen));
            return nativeOnboardingFuture;
        }

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

        // handles updating state of all controllers
        controllerManager.tick(outOfFocus);

        // handle showing/hiding mouse whilst in mixed input mode
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

        // if splitscreen ever happens this can tick over every controller
        getCurrentController().ifPresent(currentController -> {
            wrapControllerError(
                    () -> tickController(currentController, outOfFocus),
                    "Ticking current controller",
                    currentController
            );
        });
    }

    /**
     * Ticks a specific controller.
     *
     * @param controller controller to tick
     * @param outOfFocus if the window is out of focus
     */
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
            this.setCurrentController(null, true);
            consecutiveInputSwitches = 0;
            return;
        }

        if (minecraft.screen != null) {
            ScreenProcessorProvider.provide(minecraft.screen).onControllerUpdate(controller);
        }
        if (minecraft.level != null) {
            this.inGameInputHandler().ifPresent(InGameInputHandler::inputTick);
        }

        ControlifyEvents.ACTIVE_CONTROLLER_TICKED.invoker().onControllerStateUpdate(controller);
    }

    private void probeTick() {
        if (probeMode) {
            if (GLFWControllerManager.areControllersConnected()) {
                probeMode = false;
                minecraft.execute(this::finishControlifyInit);
            }
        }
    }

    public ControlifyConfig config() {
        return config;
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

    public void setCurrentController(@Nullable Controller<?, ?> controller, boolean changeInputMode) {
        if (this.currentController == controller) return;

        this.currentController = controller;

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
            config().setDirty();
        }

        this.inGameInputHandler = new InGameInputHandler(controller);

        if (controller.config().mixedInput)
            setInputMode(InputMode.MIXED);
        else if (changeInputMode)
            setInputMode(InputMode.CONTROLLER);

        config().saveIfDirty();
    }

    public Optional<ControllerManager> getControllerManager() {
        return Optional.ofNullable(controllerManager);
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
