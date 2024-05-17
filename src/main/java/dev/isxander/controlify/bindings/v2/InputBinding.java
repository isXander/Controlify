package dev.isxander.controlify.bindings.v2;

import dev.isxander.controlify.bindings.v2.input.Input;
import dev.isxander.controlify.bindings.v2.output.AnalogueOutput;
import dev.isxander.controlify.bindings.v2.output.DigitalOutput;
import dev.isxander.controlify.bindings.v2.output.GuiPressOutput;
import dev.isxander.controlify.controller.input.ControllerStateView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface InputBinding {
    ResourceLocation id();

    Component name();
    Component description();
    Component category();

    Component inputIcon();

    StateAccess createStateAccess(int historyRequired);
    StateAccess createStateAccess(int historyRequired, Consumer<StateAccess> pushEvent);
    void returnStateAccess(StateAccess stateAccess);

    void pushState(ControllerStateView state);

    void setBoundInput(Input input);
    Input boundInput();

    Input defaultInput();

    Set<BindContext> contexts();

    Optional<ResourceLocation> radialIcon();

    AnalogueOutput analogueNow();
    AnalogueOutput analoguePrev();

    DigitalOutput digitalNow();
    DigitalOutput digitalThen();

    DigitalOutput justPressed();
    DigitalOutput justReleased();
    DigitalOutput justTapped();

    GuiPressOutput guiPressed();
}
