package dev.isxander.controlify;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.compatibility.screen.ScreenProcessorProvider;
import dev.isxander.controlify.config.ControlifyConfig;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.event.ControlifyEvents;
import dev.isxander.controlify.ingame.InGameInputHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public class Controlify {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static Controlify instance = null;

    private Controller currentController;
    private InGameInputHandler inGameInputHandler;
    private InputMode currentInputMode;

    public void onInitializeInput() {
        // find already connected controllers
        for (int i = 0; i < GLFW.GLFW_JOYSTICK_LAST; i++) {
            if (GLFW.glfwJoystickPresent(i)) {
                setCurrentController(Controller.byId(i));
                LOGGER.info("Controller found: " + currentController.name());
                this.setCurrentInputMode(InputMode.CONTROLLER);
            }
        }

        // load after initial controller discovery
        ControlifyConfig.load();

        // listen for new controllers
        GLFW.glfwSetJoystickCallback((jid, event) -> {
            if (event == GLFW.GLFW_CONNECTED) {
                setCurrentController(Controller.byId(jid));
                LOGGER.info("Controller connected: " + currentController.name());
                this.setCurrentInputMode(InputMode.CONTROLLER);

                ControlifyConfig.load(); // load config again if a configuration already exists for this controller
                ControlifyConfig.save(); // save config if it doesn't exist
            } else if (event == GLFW.GLFW_DISCONNECTED) {
                var controller = Controller.CONTROLLERS.remove(jid);
                setCurrentController(Controller.CONTROLLERS.values().stream().filter(Controller::connected).findFirst().orElse(null));
                LOGGER.info("Controller disconnected: " + controller.name());
                this.setCurrentInputMode(currentController == null ? InputMode.KEYBOARD_MOUSE : InputMode.CONTROLLER);
            }
        });

        ClientTickEvents.START_CLIENT_TICK.register(this::tick);
    }

    public void tick(Minecraft client) {
        for (Controller controller : Controller.CONTROLLERS.values()) {
            controller.updateState();
        }

        ControllerState state = currentController == null ? ControllerState.EMPTY : currentController.state();

        if (state.hasAnyInput())
            this.setCurrentInputMode(InputMode.CONTROLLER);

        if (currentController == null) {
            this.setCurrentInputMode(InputMode.KEYBOARD_MOUSE);
            return;
        }

        if (client.screen != null) {
            ScreenProcessorProvider.provide(client.screen).onControllerUpdate(currentController);
        } else {
            this.getInGameInputHandler().inputTick();
        }
    }

    public Controller getCurrentController() {
        return currentController;
    }

    public void setCurrentController(Controller controller) {
        if (this.currentController == controller) return;

        this.currentController = controller;
        this.inGameInputHandler = new InGameInputHandler(controller);
    }

    public InGameInputHandler getInGameInputHandler() {
        return inGameInputHandler;
    }

    public InputMode getCurrentInputMode() {
        return currentInputMode;
    }

    public void setCurrentInputMode(InputMode currentInputMode) {
        if (this.currentInputMode == currentInputMode) return;

        this.currentInputMode = currentInputMode;
        ControlifyEvents.INPUT_MODE_CHANGED.invoker().onInputModeChanged(currentInputMode);
    }

    public static Controlify getInstance() {
        if (instance == null) instance = new Controlify();
        return instance;
    }
}
