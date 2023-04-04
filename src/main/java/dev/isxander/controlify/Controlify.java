package dev.isxander.controlify;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.logging.LogUtils;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.controller.joystick.CompoundJoystickController;
import dev.isxander.controlify.controller.sdl2.SDL2NativesManager;
import dev.isxander.controlify.gui.screen.ControllerDeadzoneCalibrationScreen;
import dev.isxander.controlify.gui.screen.VibrationOnboardingScreen;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.config.ControlifyConfig;
import dev.isxander.controlify.controller.hid.ControllerHIDService;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.ingame.guide.InGameButtonGuide;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.isxander.controlify.mixins.feature.virtualmouse.MouseHandlerAccessor;
import dev.isxander.controlify.utils.ToastUtils;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.Queue;

public class Controlify implements ControlifyApi {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static Controlify instance = null;

    private final Minecraft minecraft = Minecraft.getInstance();

    private Controller<?, ?> currentController = Controller.DUMMY;
    private InGameInputHandler inGameInputHandler;
    public InGameButtonGuide inGameButtonGuide;
    private VirtualMouseHandler virtualMouseHandler;
    private InputMode currentInputMode = InputMode.KEYBOARD_MOUSE;
    private ControllerHIDService controllerHIDService;

    private final ControlifyConfig config = new ControlifyConfig(this);

    private final Queue<Controller<?, ?>> calibrationQueue = new ArrayDeque<>();

    private int consecutiveInputSwitches = 0;
    private double lastInputSwitchTime = 0;

    private Controller<?, ?> switchableController = null;
    private double askSwitchTime = 0;
    private ToastUtils.ControlifyToast askSwitchToast = null;

    public void initializeControlify() {
        LOGGER.info("Initializing Controlify...");

        config().load();

        if (!config().globalSettings().vibrationOnboarded) {
            minecraft.setScreen(new VibrationOnboardingScreen(
                    minecraft.screen,
                    answer -> this.initializeControllers()
            ));
        } else {
            this.initializeControllers();
        }
    }

    private void initializeControllers() {
        LOGGER.info("Discovering and initializing controllers...");

        config().load();

        if (config().globalSettings().loadVibrationNatives)
            SDL2NativesManager.initialise();

        boolean dirtyControllerConfig = false;
        // find already connected controllers
        for (int jid = 0; jid <= GLFW.GLFW_JOYSTICK_LAST; jid++) {
            if (GLFW.glfwJoystickPresent(jid)) {
                try {
                    var controller = Controller.createOrGet(jid, controllerHIDService.fetchType());
                    LOGGER.info("Controller found: " + controller.name());

                    config().loadOrCreateControllerData(controller);

                    if (config().currentControllerUid().equals(controller.uid()))
                        setCurrentController(controller);

                    if (controller.config().allowVibrations && !config().globalSettings().loadVibrationNatives) {
                        controller.config().allowVibrations = false;
                        dirtyControllerConfig = true;
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to initialize controller with jid " + jid, e);
                }
            }
        }

        if (dirtyControllerConfig) {
            config().save();
        }

        checkCompoundJoysticks();

        if (Controller.CONTROLLERS.isEmpty()) {
            LOGGER.info("No controllers found.");
        }

        if (currentController() == Controller.DUMMY && config().isFirstLaunch()) {
            this.setCurrentController(Controller.CONTROLLERS.values().stream().findFirst().orElse(null));
        }

        // listen for new controllers
        GLFW.glfwSetJoystickCallback((jid, event) -> {
            try {
                if (event == GLFW.GLFW_CONNECTED) {
                    this.onControllerHotplugged(jid);
                } else if (event == GLFW.GLFW_DISCONNECTED) {
                    this.onControllerDisconnect(jid);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ClientTickEvents.START_CLIENT_TICK.register(this::tick);

        FabricLoader.getInstance().getEntrypoints("controlify", ControlifyEntrypoint.class).forEach(entrypoint -> {
            try {
                entrypoint.onControllersDiscovered(this);
            } catch (Exception e) {
                LOGGER.error("Failed to run `onControllersDiscovered` on Controlify entrypoint: " + entrypoint.getClass().getName(), e);
            }
        });
    }

    public void preInitialiseControlify() {
        LOGGER.info("Pre-initializing Controlify...");

        this.inGameInputHandler = new InGameInputHandler(Controller.DUMMY); // initialize with dummy controller before connection in case of no controller
        this.virtualMouseHandler = new VirtualMouseHandler();

        controllerHIDService = new ControllerHIDService();
        controllerHIDService.start();

        FabricLoader.getInstance().getEntrypoints("controlify", ControlifyEntrypoint.class).forEach(entrypoint -> {
            try {
                entrypoint.onControlifyPreInit(this);
            } catch (Exception e) {
                LOGGER.error("Failed to run `onControlifyPreInit` on Controlify entrypoint: " + entrypoint.getClass().getName(), e);
            }
        });
    }

    public void tick(Minecraft client) {
        if (minecraft.getOverlay() == null) {
            if (!calibrationQueue.isEmpty()) {
                Screen screen = minecraft.screen;
                while (!calibrationQueue.isEmpty()) {
                    screen = new ControllerDeadzoneCalibrationScreen(calibrationQueue.poll(), screen);
                }
                minecraft.setScreen(screen);
            }
        }

        boolean outOfFocus = !config().globalSettings().outOfFocusInput && !client.isWindowActive();

        for (var controller : Controller.CONTROLLERS.values()) {
            if (!outOfFocus)
                controller.updateState();
            else {
                controller.clearState();
                controller.rumbleManager().stopCurrentEffect();
            }
            controller.rumbleManager().tick();
        }

        ControllerState state = currentController == null ? ControllerState.EMPTY : currentController.state();

        if (switchableController != null && Blaze3D.getTime() - askSwitchTime <= 10000) {
            if (switchableController.state().hasAnyInput()) {
                this.setCurrentController(switchableController);
                if (askSwitchToast != null) {
                    askSwitchToast.remove();
                    askSwitchToast = null;
                }
                switchableController = null;
                state = ControllerState.EMPTY;
            }
        }

        if (outOfFocus)
            state = ControllerState.EMPTY;

        if (state.hasAnyInput())
            this.setInputMode(InputMode.CONTROLLER);

        if (consecutiveInputSwitches > 500) {
            LOGGER.warn("Controlify detected current controller to be constantly giving input and has been disabled.");
            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.faulty_input.title"),
                    Component.translatable("controlify.toast.faulty_input.description"),
                    true
            );
            this.setCurrentController(null);
            consecutiveInputSwitches = 0;
        }

        if (currentController == null) {
            this.setInputMode(InputMode.KEYBOARD_MOUSE);
            return;
        }

        if (client.screen != null) {
            ScreenProcessorProvider.provide(client.screen).onControllerUpdate(currentController);
        }
        if (client.level != null) {
            this.inGameInputHandler().inputTick();
        }
        this.virtualMouseHandler().handleControllerInput(currentController);

        ControlifyEvents.CONTROLLER_STATE_UPDATED.invoker().onControllerStateUpdate(currentController);
    }

    public ControlifyConfig config() {
        return config;
    }

    private void onControllerHotplugged(int jid) {
        var controller = Controller.createOrGet(jid, controllerHIDService.fetchType());
        LOGGER.info("Controller connected: " + controller.name());

        config().loadOrCreateControllerData(currentController);

        this.askToSwitchController(controller);

        checkCompoundJoysticks();
    }

    private void onControllerDisconnect(int jid) {
        Controller.CONTROLLERS.values().stream().filter(controller -> controller.joystickId() == jid).findAny().ifPresent(controller -> {
            Controller.CONTROLLERS.remove(controller.uid(), controller);

            setCurrentController(Controller.CONTROLLERS.values().stream().findFirst().orElse(null));
            LOGGER.info("Controller disconnected: " + controller.name());
            this.setInputMode(currentController == null ? InputMode.KEYBOARD_MOUSE : InputMode.CONTROLLER);

            ToastUtils.sendToast(
                    Component.translatable("controlify.toast.controller_disconnected.title"),
                    Component.translatable("controlify.toast.controller_disconnected.description", controller.name()),
                    false
            );
        });

        checkCompoundJoysticks();
    }

    private void checkCompoundJoysticks() {
        config().getCompoundJoysticks().values().forEach(info -> {
            try {
                if (info.isLoaded() && !info.canBeUsed()) {
                    LOGGER.warn("Unloading compound joystick " + info.friendlyName() + " due to missing controllers.");
                    Controller.CONTROLLERS.remove(info.type().identifier());
                }

                if (!info.isLoaded() && info.canBeUsed()) {
                    LOGGER.info("Loading compound joystick " + info.type().identifier() + ".");
                    CompoundJoystickController controller = info.attemptCreate().orElseThrow();
                    Controller.CONTROLLERS.put(info.type().identifier(), controller);
                    config().loadOrCreateControllerData(controller);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
    public @NotNull Controller<?, ?> currentController() {
        if (currentController == null)
            return Controller.DUMMY;

        return currentController;
    }

    public void setCurrentController(Controller<?, ?> controller) {
        if (controller == null)
            controller = Controller.DUMMY;

        if (this.currentController == controller) return;
        this.currentController = controller;

        if (switchableController == controller) {
            switchableController = null;
        }

        LOGGER.info("Updated current controller to " + controller.name() + "(" + controller.uid() + ")");

        if (!config().currentControllerUid().equals(controller.uid())) {
            config().save();
        }

        this.inGameInputHandler = new InGameInputHandler(controller);
        if (Minecraft.getInstance().player != null) {
            this.inGameButtonGuide = new InGameButtonGuide(controller, Minecraft.getInstance().player);
        }

        if (!controller.config().calibrated && controller != Controller.DUMMY)
            calibrationQueue.add(controller);
    }

    public InGameInputHandler inGameInputHandler() {
        return inGameInputHandler;
    }

    public InGameButtonGuide inGameButtonGuide() {
        return inGameButtonGuide;
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
    public void setInputMode(@NotNull InputMode currentInputMode) {
        if (this.currentInputMode == currentInputMode) return;
        this.currentInputMode = currentInputMode;

        var minecraft = Minecraft.getInstance();
        if (!minecraft.mouseHandler.isMouseGrabbed())
            hideMouse(currentInputMode == InputMode.CONTROLLER, true);
        if (minecraft.screen != null) {
            ScreenProcessorProvider.provide(minecraft.screen).onInputModeChanged(currentInputMode);
        }
        if (Minecraft.getInstance().player != null) {
            if (currentInputMode == InputMode.KEYBOARD_MOUSE)
                this.inGameButtonGuide = null;
            else
                this.inGameButtonGuide = new InGameButtonGuide(this.currentController != null ? currentController : Controller.DUMMY, Minecraft.getInstance().player);
        }

        if (Blaze3D.getTime() - lastInputSwitchTime < 20) {
            consecutiveInputSwitches++;
        } else {
            consecutiveInputSwitches = 0;
        }
        lastInputSwitchTime = Blaze3D.getTime();

        ControlifyEvents.INPUT_MODE_CHANGED.invoker().onInputModeChanged(currentInputMode);
    }

    public void hideMouse(boolean hide, boolean moveMouse) {
        var minecraft = Minecraft.getInstance();
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

    public static Controlify instance() {
        if (instance == null) instance = new Controlify();
        return instance;
    }
}
