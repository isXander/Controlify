package dev.isxander.controlify;

import dev.isxander.controlify.compatibility.screen.ScreenProcessorProvider;
import dev.isxander.controlify.controller.AxesState;
import dev.isxander.controlify.controller.ButtonState;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Controlify {
    private static Controlify instance = null;

    private Controller currentController;
    private InputMode currentInputMode;

    public void onInitializeInput() {
        // find already connected controllers
        for (int i = 0; i < GLFW.GLFW_JOYSTICK_LAST; i++) {
            if (GLFW.glfwJoystickPresent(i)) {
                currentController = Controller.byId(i);
                System.out.println("Connected: " + currentController.name());
                this.setCurrentInputMode(InputMode.CONTROLLER);
            }
        }

        // listen for new controllers
        GLFW.glfwSetJoystickCallback((jid, event) -> {
            System.out.println("Event: " + event);
            if (event == GLFW.GLFW_CONNECTED) {
                currentController = Controller.byId(jid);
                System.out.println("Connected: " + currentController.name());
                this.setCurrentInputMode(InputMode.CONTROLLER);
            } else if (event == GLFW.GLFW_DISCONNECTED) {
                Controller.CONTROLLERS.remove(jid);
                currentController = Controller.CONTROLLERS.values().stream().filter(Controller::connected).findFirst().orElse(null);
                System.out.println("Disconnected: " + jid);
                this.setCurrentInputMode(currentController == null ? InputMode.KEYBOARD_MOUSE : InputMode.CONTROLLER);
            }
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            updateControllers();
        });
    }

    public void updateControllers() {
        for (Controller controller : Controller.CONTROLLERS.values()) {
            controller.updateState();
        }

        ControllerState state = currentController == null ? ControllerState.EMPTY : currentController.state();

        if (state.hasAnyInput())
            this.setCurrentInputMode(InputMode.CONTROLLER);

        Minecraft client = Minecraft.getInstance();
        if (client.screen != null && currentController != null) ScreenProcessorProvider.provide(client.screen).onControllerUpdate(currentController);
    }

    public InputMode getCurrentInputMode() {
        return currentInputMode;
    }

    public void setCurrentInputMode(InputMode currentInputMode) {
        this.currentInputMode = currentInputMode;
    }

    public static Controlify getInstance() {
        if (instance == null) instance = new Controlify();
        return instance;
    }
}
