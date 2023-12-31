package dev.isxander.controlify.controller;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.hid.ControllerHIDService;
import dev.isxander.controlify.rumble.RumbleCapable;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.Log;
import org.apache.commons.lang3.SerializationUtils;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractController<S extends ControllerState, C extends ControllerConfig> implements Controller<S, C>, RumbleCapable {
    protected final int joystickId;
    protected String name;
    private final String uid;
    private final String guid;
    private final ControllerType type;
    private final ControllerHIDService.ControllerHIDInfo hidInfo;

    protected ControllerBindings<S> bindings;
    protected C config, defaultConfig;

    public AbstractController(int joystickId, ControllerHIDService.ControllerHIDInfo hidInfo) {
        this.hidInfo = hidInfo;

        this.joystickId = joystickId;
        this.guid = GLFW.glfwGetJoystickGUID(joystickId);

        if (hidInfo.hidDevice().isPresent()) {
            this.uid = hidInfo.createControllerUID().orElseThrow();
            this.type = hidInfo.type();
        } else {
            this.uid = "unidentified-guid-" + UUID.nameUUIDFromBytes(this.guid.getBytes());
            this.type = ControllerType.UNKNOWN;
        }

        var joystickName = GLFW.glfwGetJoystickName(joystickId);
        String name = type != ControllerType.UNKNOWN || joystickName == null ? type.friendlyName() : joystickName;
        setName(name);
    }

    public String name() {
        if (config().customName != null)
            return config().customName;
        return name;
    }

    protected void setName(String name) {
        String uniqueName = name;
        int i = 1;
        while (Controlify.instance().getControllerManager().orElseThrow().getConnectedControllers().stream().map(Controller::name).anyMatch(uniqueName::equalsIgnoreCase)) {
            uniqueName = name + " (" + i++ + ")";
            if (i > 1000) throw new IllegalStateException("Could not find a unique name for controller " + name + " (" + uid() + ")! (tried " + i + " times)");
        }
        this.name = uniqueName;
    }

    @Override
    public String uid() {
        return this.uid;
    }

    @Override
    public int joystickId() {
        return this.joystickId;
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
        C newConfig;
        try {
            System.out.println(new TypeToken<C>(getClass()){}.getType());
            newConfig = gson.fromJson(json, new TypeToken<C>(getClass()){}.getType());
            System.out.println(newConfig);
        } catch (Exception e) {
            Log.LOGGER.error("Could not set config for controller " + name() + " (" + uid() + ")! Using default config instead. Printing json: " + json.toString(), e);
            Controlify.instance().config().setDirty();
            return;
        }

        if (newConfig != null) {
            this.config = newConfig;
        } else {
            Log.LOGGER.error("Could not set config for controller " + name() + " (" + uid() + ")! Using default config instead.");
            this.config = SerializationUtils.clone(defaultConfig());
            Controlify.instance().config().setDirty();
        }
        this.config.validateRadialActions(bindings);
    }

    @Override
    public Optional<ControllerHIDService.ControllerHIDInfo> hidInfo() {
        return Optional.of(hidInfo);
    }

    @Override
    public RumbleState applyRumbleSourceStrength(RumbleState state, RumbleSource source) {
        float strengthMod = config().getRumbleStrength(source);
        if (source != RumbleSource.MASTER)
            strengthMod *= config().getRumbleStrength(RumbleSource.MASTER);
        return state.mul(strengthMod);
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
