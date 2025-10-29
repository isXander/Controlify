package dev.isxander.controlify.input;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.config.ValueInput;
import dev.isxander.controlify.config.ValueOutput;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.controller.impl.ConfigImpl;
import dev.isxander.controlify.controller.input.DeadzoneGroup;
import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.controller.serialization.IConfig;
import dev.isxander.controlify.input.action.*;
import dev.isxander.controlify.input.action.gesture.DefaultGestureManager;
import dev.isxander.controlify.input.action.gesture.Gesture;
import dev.isxander.controlify.input.action.gesture.NoopGesture;
import dev.isxander.controlify.input.action.gesture.builder.GestureBuilder;
import dev.isxander.controlify.input.input.DigitalAnalogueStage;
import dev.isxander.controlify.input.input.GyroPreprocessStage;
import dev.isxander.controlify.input.input.SensorType;
import dev.isxander.controlify.utils.CUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class InputComponent implements ECSComponent {
    public static final ResourceLocation ID = CUtil.rl("input");

    private final ControllerEntity controller;

    private final InputPipeline inputPipeline;
    private final SensorPipeline sensorPipeline;
    private final ActionGraph actionGraph;

    private final Map<ResourceLocation, ActionImpl> actions;

    private final Set<ResourceLocation> buttons, axes;
    // maps axis ResourceLocation to its DeadzoneGroup
    private final Map<ResourceLocation, DeadzoneGroup> deadzoneAxes;
    private final boolean isGamepad;
    private final Set<SensorType> supportedSensors;

    private final IConfig<Config> config;

    public InputComponent(
            ControllerEntity controller,
            InputPipeline inputPipeline,
            SensorPipeline sensorPipeline,
            Set<ResourceLocation> buttons,
            Set<ResourceLocation> axes,
            Set<DeadzoneGroup> deadzoneAxes,
            boolean isGamepad,
            Set<SensorType> supportedSensors
    ) {
        this.controller = controller;
        this.config = new ConfigImpl<>(Config::new, Config.class);

        this.inputPipeline = inputPipeline;
        this.sensorPipeline = sensorPipeline;
        this.actionGraph = new ActionGraph();

        this.buttons = Set.copyOf(buttons);
        this.axes = Set.copyOf(axes);
        this.deadzoneAxes = deadzoneAxes.stream()
                .flatMap(g -> g.axes().stream().map(a -> Map.entry(a, g)))
                .collect(Util.toMap());
        this.isGamepad = isGamepad;
        this.supportedSensors = Set.copyOf(supportedSensors);

        // create actions from specs
        this.actions = ActionSpecRegistryImpl.INSTANCE.getActionSpecs().stream().collect(
                LinkedHashMap::new,
                (map, spec) -> map.put(spec.id(), this.createAction(spec)),
                Map::putAll
        );
        // register actions in the action graph
        this.actions.values().forEach(this.actionGraph::addAction);

        // connect the pipeline to the action graph
        this.inputPipeline.sourceSynthesizedSignals().subscribe(this.actionGraph);

        // configure the pipeline
        this.inputPipeline.stageDeadzone().setDeadzoneSupplier(
                rl -> {
                    var group = this.deadzoneAxes.get(rl);
                    if (group == null) return 0.0f;
                    return this.config.config().deadzones.getOrDefault(group.name(), 0.0f);
                }
        );
        this.inputPipeline.stageDigitalAnalogue().setConfigSupplier(() -> this.config.config().dacConfig);

        this.sensorPipeline.stageGyroPreprocess().setPreprocessor(
                new GyroPreprocessStage.DeadzoneFilter(0.1f)
        );
    }

    public InputPipeline getInputPipeline() {
        return this.inputPipeline;
    }

    public ActionGraph getActionGraph() {
        return this.actionGraph;
    }

    public Action getAction(ResourceLocation id) {
        return this.actions.get(id);
    }

    public Set<ResourceLocation> getAllButtons() {
        return this.buttons;
    }
    public Set<ResourceLocation> getAllAxes() {
        return this.axes;
    }
    public boolean isGamepad() {
        return this.isGamepad;
    }
    public boolean supportsSensor(SensorType type) {
        return this.supportedSensors.contains(type);
    }
    public boolean supportsGyro() {
        return this.supportedSensors.stream().anyMatch(SensorType::isGyroscope);
    }
    public boolean supportsAccelerometer() {
        return this.supportedSensors.stream().anyMatch(SensorType::isAccelerometer);
    }

    public void setActionBinding(ResourceLocation actionId, Gesture gesture) {
        ActionImpl action = this.actions.get(actionId);
        if (action == null) throw new IllegalArgumentException("No action with id " + actionId);
        action.setGesture(gesture);

        if (action.isDefaultBound()) {
            this.config.config().actionBindings.remove(actionId);
        } else {
            // only encode the difference from the default gesture
            // so other properties of the gesture are preserved if unchanged
            // e.g. the repeat delay of a HoldRepeatPulseGesture if only the input is changed
            // this does mean we need to merge with default on load though
            var deltaGesture = gesture.toBuilder().delta(action.defaultGesture().toBuilder());
            this.config.config().actionBindings.put(actionId, deltaGesture);
        }
        Controlify.instance().config().setDirty();
    }

    private ActionImpl createAction(ActionSpec spec) {
        DefaultGestureManager bindsManager = Controlify.instance().defaultGestureManager();
        Gesture defaultGesture = bindsManager.getGesture(this.controller.info().type().namespace(), spec.id())
                .orElse(NoopGesture.INSTANCE);
        Gesture gesture = Optional.ofNullable(this.config.config().actionBindings.get(spec.id()))
                .flatMap(b -> defaultGesture.toBuilder().merge(b))
                .<Gesture>map(GestureBuilder::build)
                .orElse(defaultGesture);
        return new ActionImpl(spec, gesture, defaultGesture);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static class Config implements ConfigClass {
        // deadzone group name to deadzone value
        public Map<ResourceLocation, Float> deadzones;
        public DigitalAnalogueStage.Config dacConfig;
        private Map<ResourceLocation, GestureBuilder<?, ?>> actionBindings;

        @Override
        public void save(ValueOutput output, ControllerEntity controller) {
            output.put("dac", DigitalAnalogueStage.Config.CODEC, dacConfig);
            output.put("deadzones", Codec.unboundedMap(ResourceLocation.CODEC, Codec.FLOAT), deadzones);
            output.put("bindings", Codec.unboundedMap(ResourceLocation.CODEC, GestureBuilder.CODEC), actionBindings);
        }

        @Override
        public void load(ValueInput input, ControllerEntity controller) {
            this.dacConfig = input.readOr("dac", DigitalAnalogueStage.Config.CODEC, DigitalAnalogueStage.Config.DEFAULT);
            this.deadzones = input.readOr("deadzones", Codec.unboundedMap(ResourceLocation.CODEC, Codec.FLOAT), new Object2ObjectOpenHashMap<>());
            this.actionBindings = input.readOr("bindings", Codec.unboundedMap(ResourceLocation.CODEC, GestureBuilder.CODEC), new Object2ObjectOpenHashMap<>());
        }
    }
}
