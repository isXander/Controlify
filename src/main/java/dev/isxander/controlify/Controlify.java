package dev.isxander.controlify;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.gui.screen.ControllerDeadzoneCalibrationScreen;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.config.ControlifyConfig;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.controller.hid.ControllerHIDService;
import dev.isxander.controlify.event.ControlifyEvents;
import dev.isxander.controlify.ingame.guide.InGameButtonGuide;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.isxander.controlify.mixins.feature.virtualmouse.MouseHandlerAccessor;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.Queue;

public class Controlify {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static Controlify instance = null;

    private Controller currentController;
    private InGameInputHandler inGameInputHandler;
    public InGameButtonGuide inGameButtonGuide;
    private VirtualMouseHandler virtualMouseHandler;
    private InputMode currentInputMode;
    private ControllerHIDService controllerHIDService;

    private final ControlifyConfig config = new ControlifyConfig();

    private final Queue<Controller> calibrationQueue = new ArrayDeque<>();

    public void onInitializeInput() {
        Minecraft minecraft = Minecraft.getInstance();

        inGameInputHandler = new InGameInputHandler(Controller.DUMMY); // initialize with dummy controller before connection in case of no controllers
        controllerHIDService = new ControllerHIDService();

        // find already connected controllers
        for (int i = 0; i <= GLFW.GLFW_JOYSTICK_LAST; i++) {
            if (GLFW.glfwJoystickPresent(i)) {
                int jid = i;
                controllerHIDService.awaitNextController(device -> {
                    setCurrentController(Controller.create(jid, device));
                    LOGGER.info("Controller found: " + currentController.name());

                    if (!config().loadOrCreateControllerData(currentController)) {
                        calibrationQueue.add(currentController);
                    }
                });
            }
        }

        controllerHIDService.start();

        config().load();

        // listen for new controllers
        GLFW.glfwSetJoystickCallback((jid, event) -> {
            if (event == GLFW.GLFW_CONNECTED) {
                controllerHIDService.awaitNextController(device -> {
                    setCurrentController(Controller.create(jid, device));
                    LOGGER.info("Controller connected: " + currentController.name());
                    this.setCurrentInputMode(InputMode.CONTROLLER);

                    if (!config().loadOrCreateControllerData(currentController)) {
                        calibrationQueue.add(currentController);
                    }

                    minecraft.getToasts().addToast(SystemToast.multiline(
                            minecraft,
                            SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                            Component.translatable("controlify.toast.controller_connected.title"),
                            Component.translatable("controlify.toast.controller_connected.description", currentController.name())
                    ));
                });

            } else if (event == GLFW.GLFW_DISCONNECTED) {
                var controller = Controller.CONTROLLERS.remove(jid);
                if (controller != null) {
                    setCurrentController(Controller.CONTROLLERS.values().stream().filter(Controller::connected).findFirst().orElse(null));
                    LOGGER.info("Controller disconnected: " + controller.name());
                    this.setCurrentInputMode(currentController == null ? InputMode.KEYBOARD_MOUSE : InputMode.CONTROLLER);

                    minecraft.getToasts().addToast(SystemToast.multiline(
                            minecraft,
                            SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                            Component.translatable("controlify.toast.controller_disconnected.title"),
                            Component.translatable("controlify.toast.controller_disconnected.description", controller.name())
                    ));
                }
            }
        });

        this.virtualMouseHandler = new VirtualMouseHandler();

        ClientTickEvents.START_CLIENT_TICK.register(this::tick);
    }

    public void tick(Minecraft client) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.getOverlay() == null) {
            if (!calibrationQueue.isEmpty()) {
                Screen screen = minecraft.screen;
                while (!calibrationQueue.isEmpty()) {
                    screen = new ControllerDeadzoneCalibrationScreen(calibrationQueue.poll(), screen);
                }
                minecraft.setScreen(screen);

                minecraft.getToasts().addToast(SystemToast.multiline(
                        minecraft,
                        SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                        Component.translatable("controlify.toast.controller_calibration.title"),
                        Component.translatable("controlify.toast.controller_calibration.description")
                ));
            }
        }

        for (Controller controller : Controller.CONTROLLERS.values()) {
            controller.updateState();
        }

        ControllerState state = currentController == null ? ControllerState.EMPTY : currentController.state();
        if (!config().globalSettings().outOfFocusInput && !client.isWindowActive())
            state = ControllerState.EMPTY;

        if (state.hasAnyInput())
            this.setCurrentInputMode(InputMode.CONTROLLER);

        if (currentController == null) {
            this.setCurrentInputMode(InputMode.KEYBOARD_MOUSE);
            return;
        }

        if (client.screen != null) {
            ScreenProcessorProvider.provide(client.screen).onControllerUpdate(currentController);
        } else {
            this.inGameInputHandler().inputTick();
        }
        this.virtualMouseHandler().handleControllerInput(currentController);

        ControlifyEvents.CONTROLLER_STATE_UPDATED.invoker().onControllerStateUpdate(currentController);
    }

    public ControlifyConfig config() {
        return config;
    }

    public Controller currentController() {
        return currentController;
    }

    public void setCurrentController(Controller controller) {
        if (this.currentController == controller) return;
        this.currentController = controller;

        this.inGameInputHandler = new InGameInputHandler(this.currentController != null ? controller : Controller.DUMMY);
        if (Minecraft.getInstance().player != null) {
            this.inGameButtonGuide = new InGameButtonGuide(this.currentController != null ? controller : Controller.DUMMY, Minecraft.getInstance().player);
        }
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

    public InputMode currentInputMode() {
        return currentInputMode;
    }

    public void setCurrentInputMode(InputMode currentInputMode) {
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
