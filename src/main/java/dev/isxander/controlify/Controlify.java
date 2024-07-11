package dev.isxander.controlify;

import com.mojang.blaze3d.Blaze3D;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.ControlifyBindApiImpl;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.bindings.defaults.DefaultBindManager;
import dev.isxander.controlify.compatibility.ControlifyCompat;
import dev.isxander.controlify.config.GlobalSettings;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.id.ControllerTypeManager;
import dev.isxander.controlify.controller.input.ControllerState;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.HatState;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.controllermanager.GLFWControllerManager;
import dev.isxander.controlify.controllermanager.SDLControllerManager;
import dev.isxander.controlify.font.InputFontMapper;
import dev.isxander.controlify.gui.screen.*;
import dev.isxander.controlify.driver.SDL3NativesManager;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.ingame.ControllerPlayerMovement;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.platform.network.SidedNetworkApi;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.server.*;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.config.ControlifyConfig;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.gui.guide.InGameButtonGuide;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.isxander.controlify.mixins.feature.virtualmouse.MouseHandlerAccessor;
import dev.isxander.controlify.server.packets.*;
import dev.isxander.controlify.sound.ControlifyClientSounds;
import dev.isxander.controlify.utils.*;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import dev.isxander.controlify.wireless.LowBatteryNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static dev.isxander.controlify.utils.ControllerUtils.wrapControllerError;

public class Controlify implements ControlifyApi {
    private static Controlify instance = null;

    private final Minecraft minecraft = Minecraft.getInstance();

    private ControllerManager controllerManager;

    private boolean finishedInit = false;
    private boolean probeMode = false;

    private ControllerEntity currentController = null;
    private InputMode currentInputMode = InputMode.KEYBOARD_MOUSE;

    private InGameInputHandler inGameInputHandler;
    public InGameButtonGuide inGameButtonGuide;
    private VirtualMouseHandler virtualMouseHandler;
    private InputFontMapper inputFontMapper;
    private DefaultBindManager defaultBindManager;
    private ControllerTypeManager controllerTypeManager;
    private Set<BindContext> thisTickContexts;

    private ControllerHIDService controllerHIDService;

    private CompletableFuture<Boolean> nativeOnboardingFuture = null;

    private final ControlifyConfig config = new ControlifyConfig(this);

    private final Queue<ControllerSetupWizard> setupWizards = new ArrayDeque<>();
    private ControllerSetupWizard currentSetupWizard = null;
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

        CUtil.LOGGER.info("Pre-initializing Controlify...");

        this.inGameInputHandler = null; // set when the current controller changes
        this.virtualMouseHandler = new VirtualMouseHandler();

        this.inputFontMapper = new InputFontMapper();
        this.defaultBindManager = new DefaultBindManager();
        this.controllerTypeManager = new ControllerTypeManager();
        PlatformClientUtil.registerAssetReloadListener(inputFontMapper);
        PlatformClientUtil.registerAssetReloadListener(defaultBindManager);
        PlatformClientUtil.registerAssetReloadListener(controllerTypeManager);

        controllerHIDService = new ControllerHIDService();
        controllerHIDService.start();

        registerBuiltinPack("legacy_console");

        ControlifyClientSounds.init();

        ControlifyHandshake.setupOnClient();

        SidedNetworkApi.S2C().<VibrationPacket>listenForPacket(VibrationPacket.CHANNEL, packet -> {
            if (config().globalSettings().allowServerRumble) {
                getCurrentController().flatMap(ControllerEntity::rumble).ifPresent(rumble ->
                        rumble.rumbleManager().play(packet.source(), packet.createEffect()));
            }
        });
        SidedNetworkApi.S2C().<OriginVibrationPacket>listenForPacket(OriginVibrationPacket.CHANNEL, packet -> {
            if (config().globalSettings().allowServerRumble) {
                getCurrentController().flatMap(ControllerEntity::rumble).ifPresent(rumble ->
                        rumble.rumbleManager().play(packet.source(), packet.createEffect()));
            }
        });
        SidedNetworkApi.S2C().<EntityVibrationPacket>listenForPacket(EntityVibrationPacket.CHANNEL, packet -> {
            if (config().globalSettings().allowServerRumble) {
                getCurrentController().flatMap(ControllerEntity::rumble).ifPresent(rumble ->
                        rumble.rumbleManager().play(packet.source(), packet.createEffect()));
            }
        });
        SidedNetworkApi.S2C().<ServerPolicyPacket>listenForPacket(ServerPolicyPacket.CHANNEL, packet -> {
            CUtil.LOGGER.info("Connected server specified '{}' policy is {}.", packet.id(), packet.allowed() ? "ALLOWED" : "DISALLOWED");
            ServerPolicies.getById(packet.id()).set(ServerPolicy.fromBoolean(packet.allowed()));
        });

        PlatformClientUtil.registerClientDisconnected((client) -> {
            DebugLog.log("Disconnected from server, resetting server policies");
            ServerPolicies.unsetAll();
        });

        PlatformClientUtil.addHudLayer(CUtil.rl("button_guide"), (graphics, tickDelta) ->
                inGameButtonGuide().ifPresent(guide -> guide.renderHud(graphics, tickDelta)));
    }

    private void registerBuiltinPack(String id) {
        PlatformClientUtil.registerBuiltinResourcePack(
                CUtil.rl(id),
                Component.translatable("controlify.extra_pack." + id + ".name")
        );
    }

    /**
     * Called once Minecraft has completely loaded.
     * (When the loading overlay starts to fade).
     *
     * This is where controllers are usually initialised, as long
     * as one or more controllers are connected.
     */
    public void initializeControlify() {
        CUtil.LOGGER.info("Initializing Controlify...");

        config().load();

        ControlifyEvents.CONTROLLER_CONNECTED.register(event -> this.onControllerAdded(
                event.controller(), event.hotplugged(), event.newController()));
        ControlifyEvents.CONTROLLER_DISCONNECTED.register(event -> this.onControllerRemoved(event.controller()));

        ControlifyBindings.registerModdedBindings();

        PlatformClientUtil.registerPostScreenRender((screen, graphics, mouseX, mouseY, tickDelta) ->
                ControlifyApi.get().getCurrentController().ifPresent(controller -> {
                    virtualMouseHandler().renderVirtualMouse(graphics);
                    ScreenProcessorProvider.provide(screen).render(controller, graphics, tickDelta);
                }));

        PlatformMainUtil.applyToControlifyEntrypoint(entrypoint -> {
            try {
                entrypoint.onControlifyInit(this);
            } catch (Throwable e) {
                CUtil.LOGGER.error("Failed to run `onControlifyInit` on Controlify entrypoint: {}", entrypoint.getClass().getName(), e);
            }
        });

        if (config().globalSettings().quietMode) {
            // Use GLFW to probe for controllers without asking for natives
            boolean controllersConnected = GLFWControllerManager.areControllersConnected();

            if (controllersConnected) {
                ToastUtils.sendToast(
                        Component.translatable("controlify.toast.setup_in_config.title"),
                        Component.translatable(
                                "controlify.toast.setup_in_config.description",
                                Component.translatable("options.title"),
                                Component.translatable("controls.title"),
                                Component.literal("Controlify")
                        ),
                        false
                );
            } else {
                probeMode = true;
                PlatformClientUtil.registerClientTickEnded(client -> this.probeTick());
            }
        } else {
            finishControlifyInit();
        }

        // register events
        PlatformClientUtil.registerClientStopping(client -> this.controllerHIDService().stop());
    }

    /**
     * Loops through every controller slot and initialises it if it is connected.
     * This is guaranteed to be called at most once. If no controllers are connected
     * in the whole game lifecycle, this is never ran.
     */
    public void discoverControllers() {
        if (hasDiscoveredControllers) {
            CUtil.LOGGER.warn("Attempted to discover controllers twice!");
            return;
        }
        hasDiscoveredControllers = true;

        DebugLog.log("Discovering and initializing controllers...");

        controllerManager.discoverControllers();

        if (controllerManager.getConnectedControllers().isEmpty()) {
            CUtil.LOGGER.info("No controllers found.");
        }

        // if no controller is currently selected, pick one
        if (getCurrentController().isEmpty()) {
            Optional<ControllerEntity> lastUsedController = controllerManager.getConnectedControllers()
                    .stream()
                    .filter(c -> c.info().uid().equals(config().currentControllerUid()))
                    .findAny();

            if (lastUsedController.isPresent()) {
                this.setCurrentController(lastUsedController.get(), false);
            } else {
                ControllerEntity anyController = controllerManager.getConnectedControllers()
                        .stream()
                        .filter(c -> c.input().map(input -> input.config().config().deadzonesCalibrated).orElse(true)
                                || c.gyro().map(gyro -> gyro.config().config().calibrated).orElse(true))
                        .findFirst()
                        .orElse(null);

                this.setCurrentController(anyController, false);
            }
        }

        config().saveIfDirty();

        PlatformMainUtil.applyToControlifyEntrypoint(entrypoint -> {
            try {
                entrypoint.onControllersDiscovered(this);
            } catch (Throwable e) {
                CUtil.LOGGER.error("Failed to run `onControllersDiscovered` on Controlify entrypoint: {}", entrypoint.getClass().getName(), e);
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

        return askNatives().whenComplete((loaded, th) -> UnhandledCompletableFutures.run(() -> {
            CUtil.LOGGER.info("Finishing Controlify init...");

            if (!loaded) {
                CUtil.LOGGER.warn("CONTROLIFY DID NOT LOAD SDL3 NATIVES. MANY FEATURES DISABLED!");
            }

            try {
                controllerManager = loaded ? new SDLControllerManager() : new GLFWControllerManager();
            } catch (Throwable throwable) {
                CUtil.LOGGER.error("Failed to initialize controller manager", throwable);
                return;
            }

            PlatformClientUtil.registerClientTickStarted(this::tick);

            // initialise and compatability modules that controlify implements itself
            // this does NOT invoke any entrypoints. this is done in the pre-initialisation phase
            ControlifyCompat.init();

            // make sure people don't someone add binds after controllers could have been created
            ControlifyBindApiImpl.INSTANCE.lock();

            // assume that if someone explicitly went into controlify settings,
            // they have a controller and want the full experience.
            if (config().globalSettings().quietMode) {
                config().globalSettings().quietMode = false;
                config().setDirty();
            }

            discoverControllers();

            if (DebugProperties.INIT_DUMP) {
                CUtil.LOGGER.info("\n{}", DebugDump.dumpDebug());
            }
        }, minecraft)).thenApply(t -> null);
    }

    /**
     * Called when a controller is connected. Either from controller
     * discovery or hotplugging.
     *
     * @param controller the new controller
     * @param hotplugged if this was a result of hotplugging
     * @param newController if this controller has never been seen before
     */
    private void onControllerAdded(ControllerEntity controller, boolean hotplugged, boolean newController) {
        ControllerSetupWizard wizard = new ControllerSetupWizard();

        wizard.addStage(() -> SubmitUnknownControllerScreen.canSubmit(controller), nextScreen -> new SubmitUnknownControllerScreen(controller, nextScreen));

        boolean calibrated = controller.input().map(input -> input.config().config().deadzonesCalibrated).orElse(false)
                || controller.gyro().map(gyro -> gyro.config().config().calibrated).orElse(false);
        if (hotplugged && calibrated) {
            setCurrentController(controller, true);
        }

        wizard.addStage(
                () -> {
                    Optional<InputComponent> inputOpt = controller.input();
                    if (inputOpt.isPresent()) {
                        InputComponent input = inputOpt.get();
                        return !input.isDefinitelyGamepad() && input.confObj().mapping == null;
                    }
                    return false;
                },
                nextScreen -> new AskToMapControllerScreen(controller, nextScreen)
        );
        wizard.addStage(
                () -> !calibrated,
                nextScreen -> new ControllerCalibrationScreen(controller, nextScreen)
        );
        wizard.addStage(
                () -> controller.bluetooth().map(bt -> !bt.confObj().dontShowWarningAgain).orElse(false),
                nextScreen -> new BluetoothWarningScreen(controller.bluetooth().orElseThrow(), nextScreen)
        );

        if (hotplugged) {
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

        setupWizards.add(wizard);
    }

    /**
     * Called when a controller is disconnected.
     * @param controller controller that has been disconnected
     */
    private void onControllerRemoved(ControllerEntity controller) {
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
     * Asks the user if they want to download the SDL3 library,
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

        GlobalSettings settings = config().globalSettings();

        // try offline load without asking permission (because nothing is downloaded)
        if ((!settings.vibrationOnboarded || settings.loadVibrationNatives) && SDL3NativesManager.tryOfflineLoadAndStart()) {
            settings.vibrationOnboarded = true;
            settings.loadVibrationNatives = true;
            config().setDirty();

            return nativeOnboardingFuture = CompletableFuture.completedFuture(true);
        }

        // just say no if the platform doesn't support it
        if (!SDL3NativesManager.isSupportedOnThisPlatform()) {
            CUtil.LOGGER.warn("SDL is not supported on this platform. Platform: {}", SDL3NativesManager.Target.CURRENT);
            nativeOnboardingFuture = new CompletableFuture<>();
            minecraft.setScreen(new NoSDLScreen(() -> nativeOnboardingFuture.complete(false), minecraft.screen));
            return nativeOnboardingFuture;
        }

        // the user has already been asked, initialise SDL if necessary
        // and return a completed future
        if (config().globalSettings().vibrationOnboarded) {
            if (config().globalSettings().loadVibrationNatives) {
                return nativeOnboardingFuture = SDL3NativesManager.maybeLoad();
            }
            // micro-optimization. no need to create a new future every time. use the first not null check
            return nativeOnboardingFuture = CompletableFuture.completedFuture(false);
        }

        nativeOnboardingFuture = new CompletableFuture<>();

        // open the SDL onboarding screen. complete the future when the user has made their choice
        InitialScreenRegistryDuck.registerInitialScreen(runnable -> new SDLOnboardingScreen(
                runnable,
                answer -> {
                    if (answer) {
                        SDL3NativesManager.maybeLoad().whenComplete((loaded, th) -> {
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
            if (currentSetupWizard != null && currentSetupWizard.isDone()) {
                currentSetupWizard = null;
            }

            if (!setupWizards.isEmpty() && !(minecraft.screen instanceof DontInteruptScreen)) {
                currentSetupWizard = setupWizards.poll();
                minecraft.setScreen(currentSetupWizard.start(minecraft.screen));
            }
        }

        boolean outOfFocus = !config().globalSettings().outOfFocusInput && !client.isWindowActive();

        this.thisTickContexts = BindContext.REGISTRY.stream()
                .filter(ctx -> ctx.isApplicable().apply(minecraft))
                .collect(Collectors.toUnmodifiableSet());

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
    private void tickController(ControllerEntity controller, boolean outOfFocus) {
        InputComponent input = controller.input().orElseThrow();
        ControllerStateView state = input.stateNow();
        Optional<RumbleManager> rumbleManager = controller.rumble().map(RumbleComponent::rumbleManager);

        rumbleManager.ifPresent(rumble -> rumble.setSilent(outOfFocus || minecraft.isPaused() || minecraft.screen instanceof PauseScreen));
        if (outOfFocus) {
            state = ControllerState.EMPTY;
        } else {
            rumbleManager.ifPresent(RumbleManager::tick);
        }

        boolean givingInput = state.getButtons().stream().anyMatch(state::isButtonDown)
                || state.getAxes().stream().map(state::getAxisState).anyMatch(axis -> Math.abs(axis) > 0.1f)
                || state.getHats().stream().map(state::getHatState).anyMatch(hat -> hat != HatState.CENTERED);
        if (givingInput && !this.currentInputMode().isController()) {
            this.setInputMode(input.confObj().mixedInput ? InputMode.MIXED : InputMode.CONTROLLER);

            return; // don't process input if this is changing mode.
        }

        if (consecutiveInputSwitches > 100) {
            CUtil.LOGGER.warn("Controlify detected current controller to be constantly giving input and has been disabled.");
            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.faulty_input.title"),
                    Component.translatable("controlify.toast.faulty_input.description"),
                    true
            );
            this.setCurrentController(null, true);
            consecutiveInputSwitches = 0;
            return;
        }

        if (this.currentInputMode().isController()) { // only process input if in correct input mode
            if (minecraft.level != null) {
                this.inGameInputHandler().ifPresent(InGameInputHandler::inputTick);
            }
            if (minecraft.screen != null) {
                ScreenProcessorProvider.provide(minecraft.screen).onControllerUpdate(controller);
            }

            ControlifyEvents.ACTIVE_CONTROLLER_TICKED.invoke(new ControlifyEvents.ControllerStateUpdate(controller));
        }
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
    public @NotNull Optional<ControllerEntity> getCurrentController() {
        return Optional.ofNullable(currentController);
    }

    public void setCurrentController(@Nullable ControllerEntity controller, boolean changeInputMode) {
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

        DebugLog.log("Updated current controller to {}({})", controller.name(), controller.info().uid());

        if (!controller.info().uid().equals(config().currentControllerUid())) {
            config().setDirty();
        }

        this.inGameInputHandler = new InGameInputHandler(controller);
        ControllerPlayerMovement.ensureCorrectInput(minecraft.player);

        if (controller.input().map(input -> input.config().config().mixedInput).orElse(false))
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
            getCurrentController().flatMap(ControllerEntity::input).ifPresent(state -> {
                state.rawStateNow().clearState();
                state.rawStateThen().clearState();
            });
            if (minecraft.getCurrentServer() != null) {
                notifyNewServer(minecraft.getCurrentServer());
            }
        }

        ControllerPlayerMovement.updatePlayerInput(minecraft.player);

        ControlifyEvents.INPUT_MODE_CHANGED.invoke(new ControlifyEvents.InputModeChanged(currentInputMode));

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

    public InputFontMapper inputFontMapper() {
        return inputFontMapper;
    }

    public DefaultBindManager defaultBindManager() {
        return defaultBindManager;
    }

    public ControllerTypeManager controllerTypeManager() {
        return controllerTypeManager;
    }

    public Set<BindContext> thisTickBindContexts() {
        return this.thisTickContexts;
    }

    public void notifyNewServer(ServerData data) {
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
}
