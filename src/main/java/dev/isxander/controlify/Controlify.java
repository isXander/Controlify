package dev.isxander.controlify;

import com.mojang.blaze3d.Blaze3D;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.api.guide.ContainerCtx;
import dev.isxander.controlify.api.guide.GuideDomainRegistries;
import dev.isxander.controlify.api.guide.GuideDomainRegistry;
import dev.isxander.controlify.api.guide.InGameCtx;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.ControlifyBindApiImpl;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.bindings.defaults.DefaultBindManager;
import dev.isxander.controlify.compatibility.ControlifyCompat;
import dev.isxander.controlify.config.ConfigManager;
import dev.isxander.controlify.config.dto.profile.defaults.DefaultConfigManager;
import dev.isxander.controlify.config.settings.device.DeviceSettings;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.id.ControllerTypeManager;
import dev.isxander.controlify.controller.input.ControllerState;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.controllermanager.GLFWControllerManager;
import dev.isxander.controlify.controllermanager.SDLControllerManager;
import dev.isxander.controlify.driver.sdl.SDLNativesLoader;
import dev.isxander.controlify.driver.steamdeck.SteamDeckMode;
import dev.isxander.controlify.driver.steamdeck.SteamDeckUtil;
import dev.isxander.controlify.font.InputFontMapper;
import dev.isxander.controlify.gui.guide.GuideDomains;
import dev.isxander.controlify.gui.screen.*;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.ingame.ControllerPlayerMovement;
import dev.isxander.controlify.platform.client.PlatformClientUtil;
import dev.isxander.controlify.platform.main.PlatformMainUtil;
import dev.isxander.controlify.platform.network.SidedNetworkApi;
import dev.isxander.controlify.rumble.RumbleManager;
import dev.isxander.controlify.screenop.keyboard.KeyboardLayoutManager;
import dev.isxander.controlify.server.*;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
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
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.*;
import java.util.stream.Collectors;

import static dev.isxander.controlify.utils.ControllerUtils.wrapControllerError;

public class Controlify implements ControlifyApi {
    private static Controlify instance = null;

    private Minecraft minecraft = null;

    private ControllerManager controllerManager;

    private ControllerEntity currentController = null;
    private InputMode currentInputMode = InputMode.KEYBOARD_MOUSE;

    private @Nullable InGameInputHandler inGameInputHandler;
    public @Nullable InGameButtonGuide inGameButtonGuide;
    private VirtualMouseHandler virtualMouseHandler;

    // Asset reloaders / managers
    private InputFontMapper inputFontMapper;
    private DefaultBindManager defaultBindManager;
    private DefaultConfigManager defaultConfigManager;
    private ControllerTypeManager controllerTypeManager;
    private KeyboardLayoutManager keyboardLayoutManager;

    private Set<BindContext> thisTickContexts;

    private ControllerHIDService controllerHIDService;

    private ConfigManager config;

    private final Queue<ControllerSetupWizard> setupWizards = new ArrayDeque<>();
    private ControllerSetupWizard currentSetupWizard = null;
    private boolean hasDiscoveredControllers = false;

    private int consecutiveInputSwitches = 0;
    private double lastInputSwitchTime = 0;

    private int showMouseTicks = 0;

    /**
     * Called at usual fabric client entrypoint and in NeoForge mod constructor
     * Always runs, even with no controllers detected.
     * In this state, Controlify is only partially loaded, no controllers
     * have been initialised, nor has the config. This is done at {@link Controlify#initializeControlify()}.
     * This is where regular fabric callbacks and forge events should be registered.
     * On NeoForge, this is run so early that Minecraft.getInstance() has not yet been set. So extra care should be taken as to not use it.
     */
    public void preInitialiseControlify() {
        DebugProperties.printProperties();

        CUtil.LOGGER.log("Pre-initializing Controlify...");

        if (DebugProperties.MIXIN_AUDIT) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }

        this.config = new ConfigManager(
                PlatformMainUtil.getConfigDir().resolve("controlify.json")
        );

        this.inputFontMapper = new InputFontMapper();
        this.defaultBindManager = new DefaultBindManager();
        this.defaultConfigManager = new DefaultConfigManager();
        this.controllerTypeManager = new ControllerTypeManager();
        this.keyboardLayoutManager = new KeyboardLayoutManager();
        PlatformClientUtil.registerAssetReloadListener(inputFontMapper);
        PlatformClientUtil.registerAssetReloadListener(defaultBindManager);
        PlatformClientUtil.registerAssetReloadListener(defaultConfigManager);
        PlatformClientUtil.registerAssetReloadListener(controllerTypeManager);
        PlatformClientUtil.registerAssetReloadListener(keyboardLayoutManager);
        PlatformClientUtil.registerAssetReloadListener(GuideDomains.IN_GAME);
        PlatformClientUtil.registerAssetReloadListener(GuideDomains.CONTAINER);

        controllerHIDService = new ControllerHIDService();
        controllerHIDService.start();

        registerBuiltinPack("legacy_console");

        ControlifyClientSounds.init();

        ControlifyHandshake.setupOnClient();

        SidedNetworkApi.S2C().<VibrationPacket>listenForPacket(VibrationPacket.CHANNEL, packet -> {
            if (config().getSettings().globalSettings().allowServerRumble) {
                getCurrentController().flatMap(ControllerEntity::rumble).ifPresent(rumble ->
                        rumble.rumbleManager().play(packet.source(), packet.createEffect()));
            }
        });
        SidedNetworkApi.S2C().<OriginVibrationPacket>listenForPacket(OriginVibrationPacket.CHANNEL, packet -> {
            if (config().getSettings().globalSettings().allowServerRumble) {
                getCurrentController().flatMap(ControllerEntity::rumble).ifPresent(rumble ->
                        rumble.rumbleManager().play(packet.source(), packet.createEffect()));
            }
        });
        SidedNetworkApi.S2C().<EntityVibrationPacket>listenForPacket(EntityVibrationPacket.CHANNEL, packet -> {
            if (config().getSettings().globalSettings().allowServerRumble) {
                getCurrentController().flatMap(ControllerEntity::rumble).ifPresent(rumble ->
                        rumble.rumbleManager().play(packet.source(), packet.createEffect()));
            }
        });
        SidedNetworkApi.S2C().<ServerPolicyPacket>listenForPacket(ServerPolicyPacket.CHANNEL, packet -> {
            CUtil.LOGGER.log("Connected server specified '{}' policy is {}.", packet.id(), packet.allowed() ? "ALLOWED" : "DISALLOWED");
            ServerPolicies.getById(packet.id()).set(ServerPolicy.fromBoolean(packet.allowed()));
        });

        PlatformClientUtil.registerClientDisconnected((client) -> {
            DebugLog.log("Disconnected from server, resetting server policies");
            ServerPolicies.unsetAll();
        });

        PlatformClientUtil.addHudLayer(CUtil.rl("button_guide"), (graphics, deltaTracker) ->
                inGameButtonGuide().ifPresent(guide -> guide.renderHud(graphics, deltaTracker.getGameTimeDeltaPartialTick(false))));

        PlatformMainUtil.applyToControlifyEntrypoint(entrypoint -> {
            try {
                entrypoint.onControlifyPreInit(new PreInitContext() {
                    @Override
                    public ControlifyBindApi bindings() {
                        return ControlifyBindApiImpl.INSTANCE;
                    }

                    @Override
                    public GuideDomainRegistries guideRegistries() {
                        return new GuideDomainRegistries() {
                            @Override
                            public GuideDomainRegistry<InGameCtx> inGame() {
                                return GuideDomains.IN_GAME;
                            }

                            @Override
                            public GuideDomainRegistry<ContainerCtx> container() {
                                return GuideDomains.CONTAINER;
                            }
                        };
                    }
                });
            } catch (Throwable e) {
                CUtil.LOGGER.error("Failed to run `onControlifyPreInit` on Controlify entrypoint: {}", entrypoint.getClass().getName(), e);
            }
        });
        GuideDomains.freeze();
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
        CUtil.LOGGER.log("Initializing Controlify...");
        this.minecraft = Minecraft.getInstance();

        this.inGameInputHandler = null; // set when the current controller changes
        this.virtualMouseHandler = new VirtualMouseHandler();

        config().loadOrDefault();

        ControlifyEvents.CONTROLLER_CONNECTED.register(event -> this.onControllerAdded(
                event.controller(), event.hotplugged()));
        ControlifyEvents.CONTROLLER_DISCONNECTED.register(event -> this.onControllerRemoved(event.controller()));

        ControlifyBindings.registerModdedBindings();

        PlatformClientUtil.registerPostScreenRender((screen, graphics, mouseX, mouseY, tickDelta) ->
                ControlifyApi.get().getCurrentController().ifPresent(controller -> {
                    virtualMouseHandler().renderVirtualMouse(graphics);
                    ScreenProcessorProvider.provide(screen).render(controller, graphics, tickDelta);
                }));

        boolean loadedSDL = SDLNativesLoader.tryLoad();
        try {
            controllerManager = loadedSDL ? new SDLControllerManager(CUtil.LOGGER) : new GLFWControllerManager(CUtil.LOGGER);
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

        discoverControllers();

        if (DebugProperties.INIT_DUMP) {
            CUtil.LOGGER.log("\n{}", DebugDump.dumpDebug());
        }

        // register events
        PlatformClientUtil.registerClientStopping(client -> this.controllerHIDService().stop());

        if (this.config().getSettings().globalSettings().useEnhancedSteamDeckDriver) {
            doSteamDeckChecks();
        }

        PlatformMainUtil.applyToControlifyEntrypoint(entrypoint -> {
            try {
                entrypoint.onControlifyInit(new InitContext() {
                    @Override
                    public ControlifyApi controlify() {
                        return Controlify.this;
                    }
                });
            } catch (Throwable e) {
                CUtil.LOGGER.error("Failed to run `onControlifyInit` on Controlify entrypoint: {}", entrypoint.getClass().getName(), e);
            }
        });
    }

    private void doSteamDeckChecks() {
        CUtil.LOGGER.log("Steam Deck state: {}", SteamDeckUtil.DECK_MODE);

        if (!SteamDeckUtil.IS_STEAM_DECK && DebugProperties.STEAM_DECK_CUSTOM_CEF_URL == null) {
            return;
        }

        boolean connectedToCef = SteamDeckUtil.getDeckInstance().isPresent();

        if (!connectedToCef) {
            CUtil.LOGGER.error("Controlify could not connect to CEF debugger instance. Decky is probably not installed.");
            InitialScreenRegistryDuck.registerInitialScreen(SteamDeckAlerts::createDeckyRequiredWarning);
        }

        if (SteamDeckUtil.DECK_MODE == SteamDeckMode.DESKTOP_MODE) {
            CUtil.LOGGER.warn("Controlify is running in SteamOS desktop mode.");
            InitialScreenRegistryDuck.registerInitialScreen(SteamDeckAlerts::createDesktopModeWarning);
        }

        if (connectedToCef && SteamDeckUtil.DECK_MODE == SteamDeckMode.GAMING_MODE) {
            CUtil.LOGGER.log("Steam Deck is in gaming mode and Controlify has successfully connected to CEF.");
        }
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
            CUtil.LOGGER.log("No controllers found.");
        }

        // if no controller is currently selected, pick the first one
        if (this.getCurrentController().isEmpty()) {
            Optional<ControllerEntity> preferredController = controllerManager.getConnectedControllers()
                    .stream()
                    .findAny();
            this.setCurrentController(preferredController.orElse(null), false);
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
     * Called when a controller is connected. Either from controller
     * discovery or hotplugging.
     *
     * @param controller the new controller
     * @param hotplugged if this was a result of hotplugging
     */
    private void onControllerAdded(ControllerEntity controller, boolean hotplugged) {
        ControllerSetupWizard wizard = new ControllerSetupWizard();

        // wizard.addStage(() -> SubmitUnknownControllerScreen.canSubmit(controller), nextScreen -> new SubmitUnknownControllerScreen(controller, nextScreen));

        // Calibration screen removed - gyro calibration is now automatic via rolling calibration

        wizard.addStage(
                () -> {
                    Optional<InputComponent> inputOpt = controller.input();
                    if (inputOpt.isPresent()) {
                        InputComponent input = inputOpt.get();
                        DeviceSettings deviceSettings = config().getSettings().getOrCreateDeviceSettings(controller.uid());
                        return !input.isDefinitelyGamepad() && deviceSettings.mapping == null;
                    }
                    return false;
                },
                nextScreen -> new AskToMapControllerScreen(controller, nextScreen)
        );
        wizard.addStage(
                () -> controller.dualSense().isPresent() && controller.bluetooth().map(bt -> !bt.settings().dontShowWarning).orElse(false),
                nextScreen -> new BluetoothWarningScreen(controller.bluetooth().orElseThrow(), nextScreen)
        );

        if (hotplugged) {
            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.controller_connected.title"),
                    Component.translatable("controlify.toast.controller_connected.description", controller.name()),
                    false
            );
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
        if (this.getCurrentController().isPresent() && getCurrentController().get().equals(controller)) {
            this.selectFirstConnectedController();
        }

        ToastUtils.sendToast(
                Component.translatable("controlify.toast.controller_disconnected.title"),
                Component.translatable("controlify.toast.controller_disconnected.description", controller.name()),
                false
        );
    }

    private void selectFirstConnectedController() {
        Optional<ControllerEntity> firstController = controllerManager.getConnectedControllers()
                .stream()
                .findFirst();
        this.setCurrentController(firstController.orElse(null), true);
    }

    /**
     * The main loop of Controlify.
     * Only the current controller ticks.
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

        boolean outOfFocus = !config().getSettings().globalSettings().outOfFocusInput && !client.isWindowActive();

        this.thisTickContexts = BindContext.CONTEXTS.values().stream()
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

        getCurrentController().ifPresent(currentController -> {
            wrapControllerError(
                    () -> tickActiveController(currentController, outOfFocus),
                    "Ticking current controller",
                    currentController
            );
        });
        for (ControllerEntity controller : controllerManager.getConnectedControllers()) {
            if (controller.equals(getCurrentController().orElse(null))) continue;

            wrapControllerError(
                    () -> tickInactiveController(controller),
                    "Ticking inactive controller",
                    controller
            );
        }

        // Periodically save config if dirty (e.g., from gyro calibration updates)
        config.saveIfDirty();
    }

    /**
     * Ticks a specific controller.
     *
     * @param controller controller to tick
     * @param outOfFocus if the window is out of focus
     */
    private void tickActiveController(ControllerEntity controller, boolean outOfFocus) {
        InputComponent input = controller.input().orElseThrow();
        ControllerStateView state = input.stateNow();
        Optional<RumbleManager> rumbleManager = controller.rumble().map(RumbleComponent::rumbleManager);

        boolean isPaused = minecraft.isPaused() || minecraft.screen instanceof PauseScreen;
        boolean isConfigScreen = minecraft.screen instanceof YACLScreen;

        rumbleManager.ifPresent(rumble -> rumble.setSilent(outOfFocus || (isPaused && !isConfigScreen) || currentInputMode() == InputMode.KEYBOARD_MOUSE));
        if (outOfFocus) {
            state = ControllerState.EMPTY;
        } else {
            rumbleManager.ifPresent(RumbleManager::tick);
        }

        if (state.isGivingInput()) {
            //? if >=1.21.2
            minecraft.getFramerateLimitTracker().onInputReceived();

            if (!this.currentInputMode().isController()) {
                this.setInputMode(config().getSettings().globalSettings().mixedInput ? InputMode.MIXED : InputMode.CONTROLLER);

                return; // don't process input if this is changing mode.
            }
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

        if (minecraft.level != null) {
            this.inGameInputHandler().ifPresent(InGameInputHandler::inputTick);
        }

        if (this.currentInputMode().isController()) {
            if (minecraft.screen != null) {
                ScreenProcessorProvider.provide(minecraft.screen).onControllerUpdate(controller);
            }

            ControlifyEvents.ACTIVE_CONTROLLER_TICKED.invoke(new ControlifyEvents.ControllerStateUpdate(controller));
        }
    }

    private void tickInactiveController(ControllerEntity controller) {
        InputComponent input = controller.input().orElseThrow();
        ControllerStateView state = input.stateNow();

        boolean thisControllerGivingInput = state.isGivingInput();
        boolean activeControllerGivingInput = getCurrentController().map(c -> c.input().orElseThrow().stateNow().isGivingInput()).orElse(false);

        if (thisControllerGivingInput && !activeControllerGivingInput) {
            this.setCurrentController(controller, true);
        }
    }

    public ConfigManager config() {
        return config;
    }

    @Override
    public @NotNull Optional<ControllerEntity> getCurrentController() {
        return Optional.ofNullable(currentController);
    }

    public void setCurrentController(@Nullable ControllerEntity controller, boolean changeInputMode) {
        if (this.currentController == controller) return;
        this.currentController = controller;

        boolean changedInputMode = false;
        if (controller == null) {
            changedInputMode = this.setInputMode(InputMode.KEYBOARD_MOUSE);
            DebugLog.log("Cleared current controller.");
        } else {
            changedInputMode = this.setInputMode(config().getSettings().globalSettings().mixedInput ? InputMode.MIXED : InputMode.CONTROLLER);
            DebugLog.log("Updated current controller to {}({})", controller.name(), controller.uid());
        }
        if (!changedInputMode) {
            this.setupForController(controller);
        }
    }

    @Override
    public boolean setInputMode(@NotNull InputMode currentInputMode) {
        if (this.currentInputMode == currentInputMode) return false;
        if (this.currentInputMode.isController() && this.getCurrentController().isEmpty()) {
            DebugLog.log("Attempted to switch to controller input mode with no current controller set.");
            return false;
        }

        this.currentInputMode = currentInputMode;

        // Track consecutive input mode switches to prevent softlock
        if (Blaze3D.getTime() - lastInputSwitchTime < 20) {
            consecutiveInputSwitches++;
        } else {
            consecutiveInputSwitches = 0;
        }
        lastInputSwitchTime = Blaze3D.getTime();

        if (!minecraft.mouseHandler.isMouseGrabbed()) {
            hideMouse(currentInputMode.isController(), true);
        }

        this.setupForController(this.currentInputMode.isController() ? this.currentController : null);

        KeyMapping.resetToggleKeys();

        // If we have already joined a server with KB&M, then switch to controller,
        // we should do the new server notification as it won't have been triggered on join.
        if (this.currentInputMode.isController()) {
            if (minecraft.getCurrentServer() != null) {
                notifyNewServer(minecraft.getCurrentServer());
            }
        }

        // notify current screen of input mode change
        if (minecraft.screen != null) {
            ScreenProcessorProvider.provide(minecraft.screen).onInputModeChanged(currentInputMode);
        }

        // notify event listeners of input mode change
        ControlifyEvents.INPUT_MODE_CHANGED.invoke(new ControlifyEvents.InputModeChanged(currentInputMode));

        return true;
    }

    private void setupForController(@Nullable ControllerEntity controller) {
        ControllerPlayerMovement.updatePlayerInput(minecraft.player);

        if (controller == null) {
            this.inGameInputHandler = null;
            this.inGameButtonGuide = null;
            return;
        }

        this.inGameInputHandler = new InGameInputHandler(controller);
        this.inGameButtonGuide = new InGameButtonGuide(controller, this.minecraft);

        controller.input().ifPresent(input -> {
            input.rawStateNow().clearState();
            input.rawStateThen().clearState();
        });
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

    public void hideMouse(boolean hide, boolean moveMouse) {
        //? if >=1.21.9 {
        long handle = minecraft.getWindow().handle();
        //?} else {
        /*long handle = minecraft.getWindow().getWindow();
        *///?}

        GLFW.glfwSetInputMode(
                handle,
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
                mouseHandlerAccessor.invokeOnMove(handle, -50, -50);
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

    public DefaultConfigManager defaultConfigManager() {
        return defaultConfigManager;
    }

    public ControllerTypeManager controllerTypeManager() {
        return controllerTypeManager;
    }

    public KeyboardLayoutManager keyboardLayoutManager() {
        return keyboardLayoutManager;
    }

    public Set<BindContext> thisTickBindContexts() {
        return this.thisTickContexts;
    }

    public void notifyNewServer(ServerData data) {
        if (!currentInputMode().isController())
            return;

        if (config().getSettings().globalSettings().seenServers.add(data.ip)) {
            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.new_server.title"),
                    Component.translatable("controlify.toast.new_server.description", data.name),
                    true
            );
            config().saveSafely();
        }
    }

    public static Controlify instance() {
        if (instance == null) instance = new Controlify();
        return instance;
    }
}
