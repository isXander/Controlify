package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.StateAccess;
import dev.isxander.controlify.bindings.input.EmptyInput;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.bindings.output.AnalogueOutput;
import dev.isxander.controlify.bindings.output.DigitalOutput;
import dev.isxander.controlify.bindings.output.GuiPressOutput;
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
    void fakePress();

    void setBoundInput(Input input);
    Input boundInput();

    Input defaultInput();

    default boolean isUnbound() {
        return EmptyInput.equals(this.boundInput());
    }

    Set<BindContext> contexts();

    Optional<ResourceLocation> radialIcon();

    float analogueNow();
    float analoguePrev();

    boolean digitalNow();
    boolean digitalPrev();

    boolean justPressed();
    boolean justReleased();
    boolean justTapped();

    GuiPressOutput guiPressed();

    ResourceLocation ANALOGUE_NOW = new ResourceLocation("controlify", "analogue_now");
    ResourceLocation ANALOGUE_PREV = new ResourceLocation("controlify", "analogue_prev");
    ResourceLocation DIGITAL_NOW = new ResourceLocation("controlify", "digital_now");
    ResourceLocation DIGITAL_PREV = new ResourceLocation("controlify", "digital_prev");
    ResourceLocation JUST_PRESSED = new ResourceLocation("controlify", "just_pressed");
    ResourceLocation JUST_RELEASED = new ResourceLocation("controlify", "just_released");
    ResourceLocation JUST_TAPPED = new ResourceLocation("controlify", "just_tapped");
    ResourceLocation GUI_PRESSED = new ResourceLocation("controlify", "gui_pressed");
    ResourceLocation KEY_EMULATION = new ResourceLocation("controlify", "key_emulation");

    <T extends DigitalOutput> T getDigitalOutput(ResourceLocation id);
    <T extends DigitalOutput> T addDigitalOutput(ResourceLocation id, T output);

    <T extends AnalogueOutput> T getAnalogueOutput(ResourceLocation id);
    <T extends AnalogueOutput> T addAnalogueOutput(ResourceLocation id, T output);
}
