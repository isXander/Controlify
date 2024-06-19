package dev.isxander.controlify.controller.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.bindings.ControlifyBindApiImpl;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.serialization.ConfigHolder;
import dev.isxander.controlify.controller.serialization.CustomSaveLoadConfig;
import dev.isxander.controlify.controller.serialization.IConfig;
import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import dev.isxander.controlify.controller.input.mapping.ControllerMappingStorage;
import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InputComponent implements ECSComponent, ConfigHolder<InputComponent.Config>, CustomSaveLoadConfig {
    public static final ResourceLocation ID = CUtil.rl("input");

    private final ControllerEntity controller;

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
        this.controller = controller;
        this.buttonCount = buttonCount;
        this.axisCount = axisCount;
        this.hatCount = hatCount;
        this.config = new ConfigImpl<>(() -> new Config(ControllerMappingStorage.get(mappingId)), Config.class, this);
        this.definitelyGamepad = definitelyGamepad;
        this.deadzoneAxes = deadzoneAxes.stream()
                .collect(Collectors.toMap(DeadzoneGroup::name, Function.identity(), (x, y) -> y, LinkedHashMap::new));
        this.inputBindings = new LinkedHashMap<>();

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

    public Collection<InputBinding> getAllBindings() {
        return this.inputBindings.values();
    }

    public void notifyGuiPressOutputsOfNavigate() {
        for (InputBinding binding : this.inputBindings.values()) {
            binding.guiPressed().onNavigate();
        }
    }

    @Override
    public void finalise() {
        for (InputBinding binding : ControlifyBindApiImpl.INSTANCE.provideBindsForController(controller)) {
            this.inputBindings.put(binding.id(), binding);
        }
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
            if (!confObj().keepDefaultBindings && binding.boundInput().equals(binding.defaultInput()))
                continue;

            try {
                innerJson.add(
                        binding.id().toString(),
                        Input.CODEC.encodeStart(JsonOps.INSTANCE, binding.boundInput()).result().orElseThrow()
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
                // Could not find entry. Assuming defaults.
                continue;
            }

            try {
                Input input = Input.CODEC.parse(JsonOps.INSTANCE, element).result().orElseThrow();
                binding.setBoundInput(input);
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

        public boolean keepDefaultBindings = false;

        public ResourceLocation[] radialActions = new ResourceLocation[8];
        public int radialButtonFocusTimeoutTicks = 20;

        @Nullable
        public ControllerMapping mapping = null;

        @Override
        public void onConfigSaveLoad(ControllerEntity controller) {
            this.validateRadialActions(controller);
        }

        private void validateRadialActions(ControllerEntity controller) {
            boolean changed = false;
            for (int i = 0; i < radialActions.length; i++) {
                ResourceLocation action = radialActions[i];
                InputBinding radialBinding = action != null ? controller.input().orElseThrow().getBinding(action) : null;

                if (!RadialMenuScreen.EMPTY_ACTION.equals(action) && (radialBinding == null || radialBinding.radialIcon().isEmpty())) {
                    setDefaultRadialAction(i);
                    changed = true;
                }
            }
            if (changed)
                Controlify.instance().config().setDirty();
        }

        private void setDefaultRadialAction(int index) {
            radialActions[index] = switch (index) {
                case 0 -> ControlifyBindings.TOGGLE_HUD_VISIBILITY.bindId();
                case 1 -> ControlifyBindings.CHANGE_PERSPECTIVE.bindId();
                case 2 -> ControlifyBindings.DROP_STACK.bindId();
                case 3 -> ControlifyBindings.OPEN_CHAT.bindId();
                case 4 -> ControlifyBindings.SWAP_HANDS.bindId();
                case 5 -> ControlifyBindings.PICK_BLOCK.bindId();
                case 6 -> ControlifyBindings.TAKE_SCREENSHOT.bindId();
                case 7 -> ControlifyBindings.SHOW_PLAYER_LIST.bindId();
                default -> RadialMenuScreen.EMPTY_ACTION;
            };
        }
    }

}
