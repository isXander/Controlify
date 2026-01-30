package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.bindings.ControlifyBindApiImpl;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.config.settings.controller.InputSettings;
import dev.isxander.controlify.controller.*;
import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.controller.input.mapping.ControllerMapping;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InputComponent extends ECSComponentImpl {
    public static final Identifier ID = CUtil.rl("input");

    private final ControllerEntity controller;

    private ControllerState
            stateNow = ControllerState.EMPTY,
            stateThen = ControllerState.EMPTY;
    private DeadzoneControllerStateView
            deadzoneStateNow,
            deadzoneStateThen;

    private final int buttonCount, axisCount, hatCount;
    private final Map<Identifier, DeadzoneGroup> deadzoneAxes;
    private final boolean definitelyGamepad;

    private final Map<Identifier, InputBinding> inputBindings;

    public InputComponent(
            ControllerEntity controller,
            int buttonCount, int axisCount, int hatCount,
            boolean definitelyGamepad,
            Set<DeadzoneGroup> deadzoneAxes,
            String mappingId
    ) {
        this.controller = controller;
        this.buttonCount = buttonCount;
        this.axisCount = axisCount;
        this.hatCount = hatCount;
        this.definitelyGamepad = definitelyGamepad;
        this.deadzoneAxes = deadzoneAxes.stream()
                .collect(Collectors.toMap(
                        DeadzoneGroup::name,
                        Function.identity(),
                        (x, y) -> y,
                        LinkedHashMap::new
                ));
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
        ControllerMapping mapping = settings().mapping;
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

    public @Nullable InputBinding getBinding(Identifier id) {
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
    public void attach(ControllerEntity controller) {
        super.attach(controller);

        // Populate bindings
        for (InputBinding binding : ControlifyBindApiImpl.INSTANCE.provideBindsForController(controller)) {
            this.inputBindings.put(binding.id(), binding);
        }

        // Set bound inputs for bindings using config
        // We only need to do this on attach, since we never reload from disk. We only ever load at launch.
        settings().bindings.bindings.forEach((bindingId, input) -> {
            InputBinding binding = this.inputBindings.get(bindingId);
            if (binding != null) {
                binding.setBoundInput(input);
            }
        });
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

    public Map<Identifier, DeadzoneGroup> getDeadzoneGroups() {
        ControllerMapping mapping = settings().mapping;
        if (mapping != null) {
            return mapping.deadzones();
        } else {
            return this.deadzoneAxes;
        }
    }

    public InputSettings settings() {
        return this.controller().settings().input;
    }

    public InputSettings defaultSettings() {
        return this.controller().defaultSettings().input;
    }

    @Override
    public Identifier id() {
        return ID;
    }

    private void updateDeadzoneView() {
        this.deadzoneStateNow = new DeadzoneControllerStateView(this.stateNow, this);
        this.deadzoneStateThen = new DeadzoneControllerStateView(this.stateThen, this);
    }

    Optional<Identifier> getDeadzoneForAxis(Identifier axis) {
        for (DeadzoneGroup group : this.getDeadzoneGroups().values()) {
            if (group.axes().contains(axis)) {
                return Optional.of(group.name());
            }
        }
        return Optional.empty();
    }
}
