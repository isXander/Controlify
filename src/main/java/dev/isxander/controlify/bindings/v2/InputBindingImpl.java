package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.RadialIcon;
import dev.isxander.controlify.bindings.v2.inputmask.Bind;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.utils.ResizableRingBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InputBindingImpl implements InputBinding {
    private final ControllerEntity controller;
    private final ResourceLocation id;
    private final Component name, description;
    private Bind boundBind;
    private final Supplier<Bind> defaultBindSupplier;
    private final Set<BindContext> contexts;
    private final @Nullable RadialIcon radialIcon;

    private final ResizableRingBuffer<Float> stateHistory;
    private final Set<StateAccessImpl> borrowedAccesses;

    private final AnalogueOutput analogueNow;
    private final AnalogueOutput analoguePrev;
    private final DigitalOutput digitalNow;
    private final DigitalOutput digitalThen;
    private final DigitalOutput justPressed;
    private final DigitalOutput justReleased;
    private final DigitalOutput justTapped;
    private final GuiPressOutput guiPressOutput;

    public InputBindingImpl(
            ControllerEntity controller,
            ResourceLocation id,
            Component name,
            Component description,
            Bind boundBind,
            Supplier<Bind> defaultBindSupplier,
            Set<BindContext> contexts,
            @Nullable RadialIcon radialIcon
    ) {
        this.controller = controller;
        this.id = id;
        this.name = name;
        this.description = description;
        this.stateHistory = new ResizableRingBuffer<>(2, () -> 0f);
        this.boundBind = boundBind;
        this.defaultBindSupplier = defaultBindSupplier;
        this.contexts = contexts;
        this.radialIcon = radialIcon;
        this.borrowedAccesses = new HashSet<>();

        this.analogueNow = new SimpleAnalogueOutput(this, 0);
        this.analoguePrev = new SimpleAnalogueOutput(this, 1);
        this.digitalNow = new SimpleDigitalOutput(this, 0);
        this.digitalThen = new SimpleDigitalOutput(this, 1);
        this.justPressed = new JustPressedOutput(this);
        this.justReleased = new JustReleasedOutput(this);
        this.justTapped = new JustTappedOutput(this);
        this.guiPressOutput = new GuiPressOutput(this);
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
    public Component bindIcon() {
        return Controlify.instance().inputFontMapper().getComponentFromInputs(
                controller.info().type().namespace(),
                boundBind.getRelevantInputs()
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
    public void pushState(float state) {
        this.stateHistory.push(state);
        borrowedAccesses.forEach(StateAccessImpl::onPush);
    }

    @Override
    public void setBoundBind(Bind bind) {
        this.boundBind = bind;
    }

    @Override
    public Bind boundBind() {
        return this.boundBind;
    }

    @Override
    public Bind defaultBind() {
        return this.defaultBindSupplier.get();
    }

    @Override
    public Set<BindContext> contexts() {
        return this.contexts;
    }

    @Override
    public Optional<RadialIcon> radialIcon() {
        return Optional.ofNullable(this.radialIcon);
    }

    @Override
    public AnalogueOutput analogueNow() {
        return this.analogueNow;
    }

    @Override
    public AnalogueOutput analoguePrev() {
        return this.analoguePrev;
    }

    @Override
    public DigitalOutput digitalNow() {
        return this.digitalNow;
    }

    @Override
    public DigitalOutput digitalThen() {
        return this.digitalThen;
    }

    @Override
    public DigitalOutput justPressed() {
        return this.justPressed;
    }

    @Override
    public DigitalOutput justReleased() {
        return this.justReleased;
    }

    @Override
    public DigitalOutput justTapped() {
        return this.justTapped;
    }

    @Override
    public GuiPressOutput guiPressed() {
        return this.guiPressOutput;
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
