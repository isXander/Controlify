package dev.isxander.controlify.controllermanager;

import com.google.common.io.ByteStreams;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.glfw.GLFWGamepadDriver;
import dev.isxander.controlify.driver.glfw.GLFWJoystickDriver;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDIdentifier;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.stream.IntStream;

public class GLFWControllerManager extends AbstractControllerManager {

    public GLFWControllerManager() {
        this.setupCallbacks();
    }

    private void setupCallbacks() {
        GLFW.glfwSetJoystickCallback((jid, event) -> {
            try {
                GLFWUniqueControllerID ucid = new GLFWUniqueControllerID(jid);
                if (event == GLFW.GLFW_CONNECTED) {
                    tryCreate(ucid, controlify.controllerHIDService().fetchType(jid))
                            .ifPresent(controller -> onControllerConnected(controller, true));
                } else if (event == GLFW.GLFW_DISCONNECTED) {
                    getController(ucid).ifPresent(this::onControllerRemoved);
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

            UniqueControllerID ucid = new GLFWUniqueControllerID(i);

            Optional<ControllerEntity> controllerOpt = tryCreate(ucid, controlify.controllerHIDService().fetchType(i));
            controllerOpt.ifPresent(controller -> onControllerConnected(controller, false));
        }
    }

    @Override
    protected Optional<ControllerEntity> createController(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo) {
        int jid = ((GLFWUniqueControllerID) ucid).jid;

        Optional<HIDIdentifier> hid = hidInfo.hidDevice().map(HIDDevice::asIdentifier);
        String uid = hidInfo.createControllerUID(
                this.getControllerCountWithMatchingHID(hid.orElse(null))
        ).orElse("unknown-uid-" + ucid);
        boolean isGamepad = isControllerGamepad(ucid) && !DebugProperties.FORCE_JOYSTICK;
        if (isGamepad) {
            GLFWGamepadDriver driver = new GLFWGamepadDriver(jid, hidInfo.type(), uid, ucid, hidInfo.hidDevice());
            this.addController(ucid, driver.getController(), driver);
            return Optional.of(driver.getController());
        } else {
            GLFWJoystickDriver driver = new GLFWJoystickDriver(jid, hidInfo.type(), uid, ucid, hidInfo.hidDevice());
            this.addController(ucid, driver.getController(), driver);
            return Optional.of(driver.getController());
        }
    }

    @Override
    public boolean probeConnectedControllers() {
        return areControllersConnected();
    }

    @Override
    protected void loadGamepadMappings(ResourceProvider resourceProvider) {
        CUtil.LOGGER.debug("Loading gamepad mappings...");

        // GLFW uses SDL2 format
        Optional<Resource> resourceOpt = resourceProvider
                .getResource(CUtil.rl("controllers/gamecontrollerdb-sdl2.txt"));
        if (resourceOpt.isEmpty()) {
            CUtil.LOGGER.error("Failed to find game controller database.");
            return;
        }

        try (InputStream is = resourceOpt.get().open()) {
            byte[] bytes = ByteStreams.toByteArray(is);
            ByteBuffer buffer = MemoryUtil.memASCIISafe(new String(bytes));

            if (!GLFW.glfwUpdateGamepadMappings(buffer)) {
                CUtil.LOGGER.error("Failed to load gamepad mappings: {}", GLFW.glfwGetError(null));
            }
        } catch (Throwable e) {
            CUtil.LOGGER.error("Failed to load gamepad mappings: {}", e.getMessage());
        }
    }

    private Optional<ControllerEntity> getController(GLFWUniqueControllerID joystickId) {
        return controllersByUid.values().stream().filter(controller -> controller.info().ucid().equals(joystickId)).findAny();
    }

    @Override
    public boolean isControllerGamepad(UniqueControllerID ucid) {
        int joystickId = ((GLFWUniqueControllerID) ucid).jid;
        return GLFW.glfwJoystickIsGamepad(joystickId);
    }

    @Override
    protected String getControllerSystemName(UniqueControllerID ucid) {
        int joystickId = ((GLFWUniqueControllerID) ucid).jid;
        return isControllerGamepad(ucid) ? GLFW.glfwGetGamepadName(joystickId) : GLFW.glfwGetJoystickName(joystickId);
    }

    public static boolean areControllersConnected() {
        return IntStream.range(0, GLFW.GLFW_JOYSTICK_LAST + 1)
                .anyMatch(GLFW::glfwJoystickPresent);
    }

    public record GLFWUniqueControllerID(int jid) implements UniqueControllerID {
    }
}
