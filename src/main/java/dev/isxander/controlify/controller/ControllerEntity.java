package dev.isxander.controlify.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.controlify.controller.battery.BatteryLevelComponent;
import dev.isxander.controlify.controller.config.ComponentWithConfig;
import dev.isxander.controlify.controller.dualsense.DualSenseComponent;
import dev.isxander.controlify.controller.dualsense.HDHapticComponent;
import dev.isxander.controlify.controller.gyro.GyroComponent;
import dev.isxander.controlify.controller.impl.ECSEntityImpl;
import dev.isxander.controlify.controller.input.InputComponent;
import dev.isxander.controlify.controller.misc.BluetoothDeviceComponent;
import dev.isxander.controlify.controller.rumble.RumbleComponent;
import dev.isxander.controlify.controller.rumble.TriggerRumbleComponent;
import dev.isxander.controlify.controller.touchpad.TouchpadComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ControllerEntity extends ECSEntityImpl {
    private final ControllerInfo info;

    public ControllerEntity(ControllerInfo info) {
        this.info = info;

        this.setComponent(new GenericControllerComponent(this), GenericControllerComponent.ID);
    }

    public ControllerInfo info() {
        return this.info;
    }

    @NotNull
    public String name() {
        String nickname = this.generic().confObj().nickname;
        if (nickname != null)
            return nickname;

        String friendlyName = info().type().friendlyName();
        if (friendlyName != null)
            return friendlyName;

        return info().driverName();
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

    public Optional<DualSenseComponent> dualSense() {
        return this.getComponent(DualSenseComponent.ID);
    }

    public GenericControllerComponent generic() {
        return this.<GenericControllerComponent>getComponent(GenericControllerComponent.ID).orElseThrow();
    }

    public Optional<BluetoothDeviceComponent> bluetooth() {
        return this.getComponent(BluetoothDeviceComponent.ID);
    }

    public void finalise() {
        this.getAllComponents().values().forEach(ECSComponent::finalise);
    }

    public List<? extends ComponentWithConfig<?>> getAllComponentsWithConfig() {
        return this.getAllComponents().values().stream()
                .flatMap(component -> {
                    if (component instanceof ComponentWithConfig<?> config) {
                        return Stream.of(config);
                    }
                    return Stream.empty();
                })
                .toList();
    }

    public void serializeToObject(JsonObject object) {
        for (var component : this.getAllComponentsWithConfig()) {
            ResourceLocation key = component.getConfigInstance().module().id();
            JsonObject config = component.toJson();

            object.add(key.toString(), config);
        }
    }

    public void deserializeFromObject(JsonObject object) {
        for (var component : this.getAllComponentsWithConfig()) {
            ResourceLocation key = component.getConfigInstance().module().id();
            JsonElement moduleJson = object.get(key.toString());

            if (moduleJson == null) {
                CUtil.LOGGER.warn("Could not find component config {} whilst deserializing. Ignoring.", key);
                continue;
            }

            component.fromJson(moduleJson.getAsJsonObject());
        }
    }
}
