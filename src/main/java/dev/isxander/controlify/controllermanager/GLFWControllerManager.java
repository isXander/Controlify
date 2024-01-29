package dev.isxander.controlify.controllermanager;

import com.google.common.io.ByteStreams;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.stream.IntStream;

public class GLFWControllerManager extends AbstractControllerManager {
    private final Minecraft minecraft;

    public GLFWControllerManager() {
        this.minecraft = Minecraft.getInstance();

        minecraft.getResourceManager()
                .getResource(Controlify.id("controllers/gamecontrollerdb.txt"))
                .ifPresent(this::loadGamepadMappings);

        this.setupCallbacks();
    }

    private void setupCallbacks() {
        GLFW.glfwSetJoystickCallback((jid, event) -> {
            try {
                if (event == GLFW.GLFW_CONNECTED) {
                    createOrGet(jid, controlify.controllerHIDService().fetchType(jid))
                            .ifPresent(controller -> onControllerConnected(controller, true));
                } else if (event == GLFW.GLFW_DISCONNECTED) {
                    getController(jid).ifPresent(this::onControllerRemoved);
                }
            } catch (Throwable e) {
                CUtil.LOGGER.error("Failed to handle controller connect/disconnect event", e);
            }
        });
    }

    @Override
    public void discoverControllers() {
        for (int i = 0; i < GLFW.GLFW_JOYSTICK_LAST; i++) {
            if (!GLFW.glfwJoystickPresent(i))
                continue;

            Optional<Controller<?, ?>> controllerOpt = createOrGet(i, controlify.controllerHIDService().fetchType(i));
            controllerOpt.ifPresent(controller -> onControllerConnected(controller, false));
        }
    }

    @Override
    public boolean probeConnectedControllers() {
        return areControllersConnected();
    }

    @Override
    protected void loadGamepadMappings(Resource resource) {
        CUtil.LOGGER.debug("Loading gamepad mappings...");

        try (InputStream is = resource.open()) {
            byte[] bytes = ByteStreams.toByteArray(is);
            ByteBuffer buffer = MemoryUtil.memASCIISafe(new String(bytes));

            if (!GLFW.glfwUpdateGamepadMappings(buffer)) {
                CUtil.LOGGER.error("Failed to load gamepad mappings: {}", GLFW.glfwGetError(null));
            }
        } catch (Throwable e) {
            CUtil.LOGGER.error("Failed to load gamepad mappings: {}", e.getMessage());
        }
    }

    private Optional<Controller<?, ?>> getController(int joystickId) {
        return controllersByUid.values().stream().filter(controller -> controller.joystickId() == joystickId).findAny();
    }

    @Override
    public boolean isControllerGamepad(int jid) {
        return GLFW.glfwJoystickIsGamepad(jid);
    }

    @Override
    protected String getControllerSystemName(int joystickId) {
        return isControllerGamepad(joystickId) ? GLFW.glfwGetGamepadName(joystickId) : GLFW.glfwGetJoystickName(joystickId);
    }

    public static boolean areControllersConnected() {
        return IntStream.range(0, GLFW.GLFW_JOYSTICK_LAST + 1)
                .anyMatch(GLFW::glfwJoystickPresent);
    }
}
