package dev.isxander.controlify.controllermanager;

import com.google.common.io.ByteStreams;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.info.ControllerInfo;
import dev.isxander.controlify.controller.info.UIDComponent;
import dev.isxander.controlify.debug.DebugProperties;
import dev.isxander.controlify.driver.ComponentAdderDriver;
import dev.isxander.controlify.driver.CompoundDriver;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.driver.glfw.GLFWGamepadDriver;
import dev.isxander.controlify.driver.glfw.GLFWJoystickDriver;
import dev.isxander.controlify.driver.steamdeck.SteamDeckDriver;
import dev.isxander.controlify.driver.steamdeck.SteamDeckUtil;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.hid.HIDDevice;
import dev.isxander.controlify.hid.HIDID;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.log.ControlifyLogger;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class GLFWControllerManager extends AbstractControllerManager {
    private boolean steamDeckConsumed = false;

    public GLFWControllerManager(ControlifyLogger logger) {
        super(logger);
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
    protected Optional<ControllerEntity> createController(UniqueControllerID ucid, ControllerHIDService.ControllerHIDInfo hidInfo, ControlifyLogger controllerLogger) {
        int jid = ((GLFWUniqueControllerID) ucid).jid;

        boolean isGamepad = isControllerGamepad(ucid) && !DebugProperties.FORCE_JOYSTICK;

        List<Driver> drivers = new ArrayList<>();
        if ((SteamDeckUtil.DECK_MODE.isGamingMode() || DebugProperties.STEAM_DECK_CUSTOM_CEF_URL != null)
            && !steamDeckConsumed
            && hidInfo.type().namespace().equals(SteamDeckUtil.STEAM_DECK_NAMESPACE)
        ) {
            Optional<SteamDeckDriver> steamDeckDriver = SteamDeckDriver.create(controllerLogger);
            if (steamDeckDriver.isPresent()) {
                drivers.add(steamDeckDriver.get());
                steamDeckConsumed = true;
            }
        }

        if (isGamepad) {
            drivers.add(new GLFWGamepadDriver(jid));
        } else {
            drivers.add(new GLFWJoystickDriver(jid));
        }

        Optional<HIDID> hid = hidInfo.hidDevice().map(HIDDevice::asIdentifier);
        String uid = hidInfo.createControllerUID(
                this.getControllerCountWithMatchingHID(hid.orElse(null))
        ).orElse("unknown-uid-" + ucid);
        // to save giving GLFW drivers the HID info, we'll just add the UID component here at the very
        // bottom of the driver priority list
        drivers.add(new ComponentAdderDriver(controller -> {
            controller.setComponent(new UIDComponent(uid));
        }));

        CompoundDriver compoundDriver = new CompoundDriver(drivers);

        ControllerInfo info = new ControllerInfo(ucid, hidInfo.type(), hidInfo.hidDevice());
        ControllerEntity controller = new ControllerEntity(
                info,
                compoundDriver,
                this.controlify.config().getSettings().getOrCreateProfileSettings(info.type().namespace()),
                ProfileSettings.createDefault(info.type().namespace()),
                controllerLogger
        );

        addController(ucid, controller);
        return Optional.of(controller);
    }

    @Override
    public boolean probeConnectedControllers() {
        return areControllersConnected();
    }

    @Override
    protected void loadGamepadMappings(ResourceProvider resourceProvider) {
        CUtil.LOGGER.debugLog("Loading gamepad mappings...");

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
