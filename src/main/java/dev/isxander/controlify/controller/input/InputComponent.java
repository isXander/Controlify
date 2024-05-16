package dev.isxander.controlify.controller.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.v2.ControlifyBindApiImpl;
import dev.isxander.controlify.bindings.v2.InputBinding;
import dev.isxander.controlify.bindings.v2.inputmask.Bind;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.serialization.ConfigHolder;
import dev.isxander.controlify.controller.serialization.CustomSaveLoadConfig;
import dev.isxander.controlify.controller.serialization.IConfig;
import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import dev.isxander.controlify.controller.input.mapping.ControllerMappingStorage;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InputComponent implements ECSComponent, ConfigHolder<InputComponent.Config>, CustomSaveLoadConfig {
    public static final ResourceLocation ID = Controlify.id("input");

    private ControllerState
            stateNow = ControllerState.EMPTY,
            stateThen = ControllerState.EMPTY;
    private DeadzoneControllerStateView deadzoneStateNow, deadzoneStateThen;

    private final int buttonCount, axisCount, hatCount;
    private final Map<ResourceLocation, DeadzoneGroup> deadzoneAxes;
    private final boolean definitelyGamepad;

    private final Map<ResourceLocation, InputBinding> inputBindings;

    private final IConfig<Config> config;

    public InputComponent(ControllerEntity controller, int buttonCount, int axisCount, int hatCount, boolean definitelyGamepad, Set<DeadzoneGroup> deadzoneAxes, String mappingId) {
        this.buttonCount = buttonCount;
        this.axisCount = axisCount;
        this.hatCount = hatCount;
        this.config = new ConfigImpl<>(() -> new Config(ControllerMappingStorage.get(mappingId)), Config.class, this);
        this.definitelyGamepad = definitelyGamepad;
        this.deadzoneAxes = deadzoneAxes.stream()
                .collect(Collectors.toMap(DeadzoneGroup::name, Function.identity(), (x, y) -> y, LinkedHashMap::new));
        this.inputBindings = new LinkedHashMap<>();
        for (InputBinding binding : ControlifyBindApiImpl.INSTANCE.provideBindsForController(controller)) {
            this.inputBindings.put(binding.id(), binding);
        }

        this.updateDeadzoneView();
    }

    public ControllerStateView stateNow() {
        return this.deadzoneStateNow;
    }
    public ControllerStateView stateThen() {
        return this.deadzoneStateThen;
    }
    
    public ControllerState rawStateNow() {
        return this.stateNow;
    }

    public ControllerState rawStateThen() {
        return this.stateThen;
    }

    public void pushState(ControllerState state) {
        ControllerMapping mapping = confObj().mapping;
        if (mapping != null) {
            state = mapping.mapState(state);
        }

        this.stateThen = this.stateNow;
        this.stateNow = state;
        this.updateDeadzoneView();

        for (InputBinding binding : this.inputBindings.values()) {
            binding.pushState(this.deadzoneStateNow);
        }
    }

    public @Nullable InputBinding getBinding(ResourceLocation id) {
        return this.inputBindings.get(id);
    }

    public int buttonCount() {
        return this.buttonCount;
    }

    public int axisCount() {
        return this.axisCount;
    }
    public int hatCount() {
        return this.hatCount;
    }

    public boolean isDefinitelyGamepad() {
        return this.definitelyGamepad;
    }

    public Map<ResourceLocation, DeadzoneGroup> getDeadzoneGroups() {
        ControllerMapping mapping = confObj().mapping;
        if (mapping != null) {
            return mapping.deadzones();
        } else {
            return this.deadzoneAxes;
        }
    }

    @Override
    public IConfig<Config> config() {
        return this.config;
    }

    private void updateDeadzoneView() {
        this.deadzoneStateNow = new DeadzoneControllerStateView(this.stateNow, this);
        this.deadzoneStateThen = new DeadzoneControllerStateView(this.stateThen, this);
    }

    Optional<ResourceLocation> getDeadzoneForAxis(ResourceLocation axis) {
        for (DeadzoneGroup group : this.getDeadzoneGroups().values()) {
            if (group.axes().contains(axis)) {
                return Optional.of(group.name());
            }
        }
        return Optional.empty();
    }

    @Override
    public void toJson(JsonObject json) {
        JsonObject innerJson = new JsonObject();

        for (InputBinding binding : this.inputBindings.values()) {
            try {
                innerJson.add(
                        binding.id().toString(),
                        Bind.CODEC.encodeStart(JsonOps.INSTANCE, binding.boundBind()).getOrThrow()
                );
            } catch (Exception e) {
                CUtil.LOGGER.error("Failed to serialize input binding {}", binding.id(), e);
            }
        }

        json.add("bindings", innerJson);
    }

    @Override
    public void fromJson(JsonObject json) {
        if (!json.has("bindings")) {
            CUtil.LOGGER.warn("Could not find bindings in json, upgrading from older version?");
            return;
        }

        JsonObject innerJson = json.getAsJsonObject("bindings");

        for (InputBinding binding : this.inputBindings.values()) {
            JsonElement element = innerJson.get(binding.id().toString());
            if (element == null) {
                CUtil.LOGGER.warn("Could not find entry for {}. Assuming defaults.", binding.id());
                continue;
            }

            try {
                Bind bind = Bind.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
                binding.setBoundBind(bind);
            } catch (Exception e) {
                CUtil.LOGGER.error("Failed to deserialize input binding {}. Using default.", binding.id(), e);
            }
        }
    }

    public static class Config implements ConfigClass {
        public Config() {
            // for gson
        }

        public Config(@Nullable ControllerMapping typeProvidedMapping) {
            this.mapping = typeProvidedMapping;
        }

        public float hLookSensitivity = 1f;
        public float vLookSensitivity = 0.9f;
        public float virtualMouseSensitivity = 1f;
        public boolean reduceAimingSensitivity = true;

        public float buttonActivationThreshold = 0.5f;

        public Map<ResourceLocation, Float> deadzones = new Object2ObjectOpenHashMap<>();
        public boolean deadzonesCalibrated = false;
        public boolean delayedCalibration = false;

        public boolean mixedInput = false;

        @Nullable
        public ControllerMapping mapping = null;
    }

}
