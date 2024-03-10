package dev.isxander.controlify.controller;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.hdhaptic.HDHapticComponent;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import dev.isxander.controlify.controller.impl.ECSEntityImpl;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controller.rumble.TriggerRumbleComponent;
import dev.isxander.controlify.controller.touchpad.TouchpadComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.SerializationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ControllerEntity extends ECSEntityImpl {
    private final ControllerInfo info;
    private ControllerBindings bindings;

    public ControllerEntity(ControllerInfo info) {
        this.info = info;

        this.setComponent(new ConfigImpl<>(GenericControllerConfig::new, GenericControllerConfig.class), GenericControllerConfig.ID);
    }

    public ControllerInfo info() {
        return this.info;
    }

    public String name() {
        String nickname = this.genericConfig().config().nickname;
        if (nickname != null)
            return nickname;
        return Strings.nullToEmpty(info().type().friendlyName());
    }

    public Optional<InputComponent> input() {
        return this.getComponent(InputComponent.ID);
    }

    public Optional<RumbleComponent> rumble() {
        return this.getComponent(RumbleComponent.ID);
    }

    public Optional<TriggerRumbleComponent> triggerRumble() {
        return this.getComponent(TriggerRumbleComponent.ID);
    }

    public Optional<GyroComponent> gyro() {
        return this.getComponent(GyroComponent.ID);
    }

    public Optional<TouchpadComponent> touchpad() {
        return this.getComponent(TouchpadComponent.ID);
    }

    public Optional<BatteryLevelComponent> batteryLevel() {
        return this.getComponent(BatteryLevelComponent.ID);
    }

    public Optional<HDHapticComponent> hdHaptics() {
        return this.getComponent(HDHapticComponent.ID);
    }

    public IConfig<GenericControllerConfig> genericConfig() {
        return this.<IConfig<GenericControllerConfig>>getComponent(GenericControllerConfig.ID).orElseThrow();
    }

    public Optional<IConfig<GamepadControllerConfig>> gamepadConfig() {
        return this.getComponent(GamepadControllerConfig.ID);
    }

    public Optional<IConfig<JoystickControllerConfig>> joystickConfig() {
        return this.getComponent(JoystickControllerConfig.ID);
    }

    public ControllerBindings bindings() {
        return this.bindings;
    }

    public void finalise() {
        this.bindings = new ControllerBindings(this);
    }

    public Map<ResourceLocation, IConfig<?>> getAllConfigs() {
        Map<ResourceLocation, IConfig<?>> configs = new HashMap<>();

        this.getAllComponents().forEach((id, component) -> {
            if (component instanceof IConfig<?> config) {
                configs.put(id, config);
            }
            if (component instanceof ConfigHolder<?> configHolder) {
                configs.put(id, configHolder.config());
            }
        });

        return configs;
    }

    public void serializeToObject(JsonObject object, Gson gson) throws SerializationException {
        for (var entry : this.getAllConfigs().entrySet()) {
            ResourceLocation key = entry.getKey();
            IConfig<?> config = entry.getValue();

            object.add(key.toString(), config.serialize(gson, this));
        }
    }

    public void deserializeFromObject(JsonObject object, Gson gson) throws SerializationException {
        for (var entry : this.getAllConfigs().entrySet()) {
            ResourceLocation key = entry.getKey();
            IConfig<?> config = entry.getValue();

            JsonElement element = object.get(key.toString());
            config.deserialize(element, gson, this);
        }
    }

    public void resetToDefaultConfig() {
        for (var config : this.getAllConfigs().values()) {
            config.resetToDefault();
        }
    }
}
