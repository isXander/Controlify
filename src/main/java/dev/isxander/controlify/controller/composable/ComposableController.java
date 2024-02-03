package dev.isxander.controlify.controller.composable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.ControllerConfig;
import dev.isxander.controlify.controller.ControllerType;
import dev.isxander.controlify.controllermanager.UniqueControllerID;
import dev.isxander.controlify.driver.Driver;
import dev.isxander.controlify.rumble.RumbleManager;

import java.util.Set;

public class ComposableController<C extends ControllerConfig> implements Controller<C> {
    private final ComposableControllerInfo info;
    private final ComposableControllerStateProvider stateProvider;
    private final ComposableControllerConfig<C> config;
    private final ComposableControllerRumble rumble;
    private final ControllerType type;

    private final String name;
    private final ControllerBindings bindings;

    private final Set<Driver> uniqueDrivers;

    public ComposableController(
            ComposableControllerInfo info,
            ComposableControllerStateProvider stateProvider,
            ComposableControllerConfig<C> config,
            ComposableControllerRumble rumble,
            ControllerType type,
            Set<Driver> uniqueDrivers) {
        this.info = info;
        this.stateProvider = stateProvider;
        this.config = config;
        this.rumble = rumble;
        this.type = type;

        this.rumble.bindController(this);

        this.name = info.createName(this);
        this.bindings = new ControllerBindings(this);

        this.uniqueDrivers = uniqueDrivers;
    }

    @Override
    public String uid() {
        return info.uid();
    }

    @Override
    public UniqueControllerID joystickId() {
        return info.ucid();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ControllerBindings bindings() {
        return bindings;
    }

    @Override
    public ComposableControllerState state() {
        return stateProvider.stateNow();
    }

    @Override
    public ComposableControllerState prevState() {
        return stateProvider.stateThen();
    }

    @Override
    public void updateState() {
        uniqueDrivers.forEach(Driver::update);

        stateProvider.updateState(config());
    }

    @Override
    public void clearState() {
        stateProvider.clearState();
    }

    @Override
    public C config() {
        return config.config();
    }

    @Override
    public C defaultConfig() {
        return config.defaultConfig();
    }

    @Override
    public void resetConfig() {
        config.resetConfig();
    }

    @Override
    public void setConfig(Gson gson, JsonElement json) {
        config.setConfig(gson, json, this);
    }

    @Override
    public ControllerType type() {
        return type;
    }

    @Override
    public RumbleManager rumbleManager() {
        return rumble.rumbleManager();
    }

    @Override
    public boolean supportsRumble() {
        return rumble.supportsRumble();
    }

    @Override
    public boolean supportsGyro() {
        return true; // TODO
    }

    @Override
    public int buttonCount() {
        return stateProvider.buttonCount();
    }

    @Override
    public int axisCount() {
        return stateProvider.axisCount();
    }

    @Override
    public int hatCount() {
        return stateProvider.hatCount();
    }

    @Override
    public String kind() {
        return "Composable";
    }

    @Override
    public void close() {
        stateProvider.close();
        info.close();
        config.close();
        rumble.close();
    }
}
