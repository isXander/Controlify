package dev.isxander.controlify.controller;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.hid.HIDIdentifier;
import org.hid4java.HidDevice;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractController<S extends ControllerState, C extends ControllerConfig> implements Controller<S, C> {
    private final int joystickId;
    protected String name;
    private final String uid;
    private final String guid;
    private final ControllerType type;

    private final ControllerBindings<S> bindings;
    protected C config, defaultConfig;

    public AbstractController(int joystickId, @Nullable HidDevice hidDevice) {
        if (joystickId > GLFW.GLFW_JOYSTICK_LAST || joystickId < 0)
            throw new IllegalArgumentException("Joystick ID " + joystickId + " is out of range!");
        if (!GLFW.glfwJoystickPresent(joystickId))
            throw new IllegalArgumentException("Joystick " + joystickId + " is not present and cannot be initialised!");

        this.joystickId = joystickId;
        this.guid = GLFW.glfwGetJoystickGUID(joystickId);

        if (hidDevice != null) {
            this.uid = UUID.nameUUIDFromBytes(hidDevice.getPath().getBytes()).toString();
            this.type = ControllerType.getTypeForHID(new HIDIdentifier(hidDevice.getVendorId(), hidDevice.getProductId()));
        } else {
            this.uid = "unidentified-guid-" + UUID.nameUUIDFromBytes(this.guid.getBytes());
            this.type = ControllerType.UNKNOWN;
        }

        var joystickName = GLFW.glfwGetJoystickName(joystickId);
        String name = type != ControllerType.UNKNOWN || joystickName == null ? type.friendlyName() : joystickName;
        setName(name);

        this.bindings = new ControllerBindings<>(this);
    }

    @Override
    public int joystickId() {
        return this.joystickId;
    }

    public String name() {
        if (config().customName != null)
            return config().customName;
        return name;
    }

    protected void setName(String name) {
        String uniqueName = name;
        int i = 0;
        while (Controller.CONTROLLERS.values().stream().map(Controller::name).anyMatch(name::equalsIgnoreCase)) {
            uniqueName = name + " (" + i++ + ")";
        }
        this.name = uniqueName;
    }

    @Override
    public String uid() {
        return this.uid;
    }

    @Override
    public String guid() {
        return this.guid;
    }

    @Override
    public ControllerType type() {
        return this.type;
    }

    @Override
    public ControllerBindings<S> bindings() {
        return this.bindings;
    }

    @Override
    public C config() {
        return this.config;
    }

    @Override
    public C defaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public void resetConfig() {
        this.config = defaultConfig();
    }

    @Override
    public void setConfig(Gson gson, JsonElement json) {
        C newConfig = gson.fromJson(json, new TypeToken<C>(getClass()){}.getType());
        if (newConfig != null) {
            this.config = newConfig;
        } else {
            Controlify.LOGGER.error("Could not set config for controller " + name() + " (" + uid() + ")! Using default config instead.");
            this.config = defaultConfig();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractController<?, ?> that = (AbstractController<?, ?>) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}
