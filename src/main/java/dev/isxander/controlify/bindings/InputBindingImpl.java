package dev.isxander.controlify.bindings;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.bindings.output.*;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.utils.ResizableRingBuffer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InputBindingImpl implements InputBinding {
    private final ControllerEntity controller;
    private final ResourceLocation id;
    private final Component name, description, category;
    private Input boundInput;
    private final Supplier<Input> defaultBindSupplier;
    private final Set<BindContext> contexts;
    private final @Nullable ResourceLocation radialIcon;

    private final ResizableRingBuffer<Float> stateHistory;
    private final Set<StateAccessImpl> borrowedAccesses;

    private boolean suppressed;

    private final AnalogueOutput analogueNow;
    private final AnalogueOutput analoguePrev;
    private final DigitalOutput digitalNow;
    private final DigitalOutput digitalThen;
    private final DigitalOutput justPressed;
    private final DigitalOutput justReleased;
    private final DigitalOutput justTapped;
    private final GuiPressOutput guiPressOutput;

    private final Map<ResourceLocation, DigitalOutput> digitalOutputs;
    private final Map<ResourceLocation, AnalogueOutput> analogueOutputs;

    private int fakePressState = -1;

    public InputBindingImpl(
            ControllerEntity controller,
            ResourceLocation id,
            Component name,
            Component description,
            Component category,
            Supplier<Input> defaultBindSupplier,
            Set<BindContext> contexts,
            @Nullable ResourceLocation radialIcon
    ) {
        this.controller = controller;
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.stateHistory = new ResizableRingBuffer<>(2, () -> 0f);
        this.boundInput = defaultBindSupplier.get();
        this.defaultBindSupplier = defaultBindSupplier;
        this.contexts = contexts;
        this.radialIcon = radialIcon;
        this.borrowedAccesses = new HashSet<>();

        this.digitalOutputs = new HashMap<>();
        this.analogueOutputs = new HashMap<>();

        this.analogueNow = addAnalogueOutput(ANALOGUE_NOW, new SimpleAnalogueOutput(this, 0));
        this.analoguePrev = addAnalogueOutput(ANALOGUE_PREV, new SimpleAnalogueOutput(this, 1));
        this.digitalNow = addDigitalOutput(DIGITAL_NOW, new SimpleDigitalOutput(this, 0));
        this.digitalThen = addDigitalOutput(DIGITAL_PREV, new SimpleDigitalOutput(this, 1));
        this.justPressed = addDigitalOutput(JUST_PRESSED, new JustPressedOutput(this));
        this.justReleased = addDigitalOutput(JUST_RELEASED, new JustReleasedOutput(this));
        this.justTapped = addDigitalOutput(JUST_TAPPED, new JustTappedOutput(this));
        this.guiPressOutput = addDigitalOutput(GUI_PRESSED, new GuiPressOutput(this));
    }

    @Override
    public ResourceLocation id() {
        return this.id;
    }

    @Override
    public Component name() {
        return this.name;
    }

    @Override
    public Component description() {
        return this.description;
    }

    @Override
    public Component category() {
        return this.category;
    }

    @Override
    public Component inputIcon() {
        return Controlify.instance().inputFontMapper().getComponentFromInputs(
                controller.info().type().namespace(),
                boundInput.getRelevantInputs()
        );
    }

    @Override
    public StateAccess createStateAccess(int historyRequired) {
        return createStateAccess(historyRequired, null);
    }

    @Override
    public StateAccess createStateAccess(int historyRequired, Consumer<StateAccess> pushEvent) {
        if (historyRequired > this.stateHistory.size()) {
            this.stateHistory.setSize(historyRequired);
        }

        StateAccessImpl access = new StateAccessImpl(historyRequired, pushEvent);
        borrowedAccesses.add(access);

        return access;
    }

    @Override
    public void returnStateAccess(StateAccess stateAccess) {
        if (stateAccess instanceof StateAccessImpl accessImpl) {
            boolean removed = this.borrowedAccesses.remove(accessImpl);
            accessImpl.retire();

            if (removed) {
                OptionalInt newMaxSize = this.borrowedAccesses.stream().mapToInt(StateAccessImpl::maxHistory).max();
                newMaxSize.ifPresent(this.stateHistory::setSize);
            }
        } else {
            throw new IllegalStateException("Unknown implementation of state access");
        }
    }

    @Override
    public void pushState(ControllerStateView state) {
        if (!this.contexts.isEmpty()) {
            Set<BindContext> thisTickContexts = Controlify.instance().thisTickBindContexts();
            this.suppressed = this.contexts.stream().noneMatch(thisTickContexts::contains);
        } else {
            this.suppressed = false;
        }

        float analogue = this.boundInput.state(state);

        switch (fakePressState) {
            case 0 -> analogue = 0;
            case 1,2 -> analogue = 1;
            case 3 -> analogue = 0;
        }
        if (fakePressState >= 0) {
            if (fakePressState == 3)
                fakePressState = -1;
            else
                fakePressState++;
        }

        this.stateHistory.push(analogue);
        borrowedAccesses.forEach(StateAccessImpl::onPush);
    }

    @Override
    public void fakePress() {
        fakePressState = 0;
    }

    @Override
    public void setBoundInput(Input input) {
        this.boundInput = input;
        Controlify.instance().config().setDirty();
    }

    @Override
    public Input boundInput() {
        return this.boundInput;
    }

    @Override
    public Input defaultInput() {
        return this.defaultBindSupplier.get();
    }

    @Override
    public Set<BindContext> contexts() {
        return this.contexts;
    }

    @Override
    public Optional<ResourceLocation> radialIcon() {
        return Optional.ofNullable(this.radialIcon);
    }

    @Override
    public float analogueNow() {
        return this.analogueNow.get();
    }

    @Override
    public float analoguePrev() {
        return this.analoguePrev.get();
    }

    @Override
    public boolean digitalNow() {
        return this.digitalNow.get();
    }

    @Override
    public boolean digitalPrev() {
        return this.digitalThen.get();
    }

    @Override
    public boolean justPressed() {
        return this.justPressed.get();
    }

    @Override
    public boolean justReleased() {
        return this.justReleased.get();
    }

    @Override
    public boolean justTapped() {
        return this.justTapped.get();
    }

    @Override
    public GuiPressOutput guiPressed() {
        return this.guiPressOutput;
    }

    @Override
    public <T extends DigitalOutput> T addDigitalOutput(ResourceLocation id, T output) {
        this.digitalOutputs.put(id, output);
        return output;
    }

    @Override
    public <T extends DigitalOutput> T getDigitalOutput(ResourceLocation id) {
        return (T) this.digitalOutputs.get(id);
    }

    @Override
    public <T extends AnalogueOutput> T addAnalogueOutput(ResourceLocation id, T output) {
        this.analogueOutputs.put(id, output);
        return output;
    }

    @Override
    public <T extends AnalogueOutput> T getAnalogueOutput(ResourceLocation id) {
        return (T) this.analogueOutputs.get(id);
    }

    private class StateAccessImpl implements StateAccess {
        private final int maxHistory;
        private boolean valid;
        private final @Nullable Consumer<StateAccess> pushListener;

        public StateAccessImpl(int maxHistory, @Nullable Consumer<StateAccess> pushListener) {
            this.maxHistory = maxHistory;
            this.valid = true;
            this.pushListener = pushListener;
        }

        @Override
        public float analogue(int history) {
            if (!valid) throw new IllegalStateException("Tried to access state from returned access!");
            if (history > maxHistory) throw new IllegalStateException("Overflowing history!");

            return InputBindingImpl.this.stateHistory.tail(history);
        }

        @Override
        public boolean digital(int history) {
            return analogue(history) > controller.input().orElseThrow().confObj().buttonActivationThreshold;
        }

        @Override
        public boolean isSuppressed() {
            return suppressed;
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public int maxHistory() {
            return this.maxHistory;
        }

        public void onPush() {
            if (this.pushListener != null)
                this.pushListener.accept(this);
        }

        public void retire() {
            this.valid = false;
        }
    }
}
