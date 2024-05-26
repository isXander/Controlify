package dev.isxander.controlify.api.bind;

import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.StateAccess;
import dev.isxander.controlify.bindings.input.EmptyInput;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.bindings.output.AnalogueOutput;
import dev.isxander.controlify.bindings.output.DigitalOutput;
import dev.isxander.controlify.bindings.output.GuiPressOutput;
import dev.isxander.controlify.controller.input.ControllerStateView;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * An input binding is a single action that can be performed in Minecraft, e.g. punching.
 * It is bound to a controller {@link Input}, e.g. the A button.
 * Every tick, the input binding will update its state based on the controller input and then propagate to its outputs,
 * e.g. {@link #justTapped()}. The outputs are then used to perform the action in the game.
 */
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

    ResourceLocation ANALOGUE_NOW = CUtil.rl("analogue_now");
    ResourceLocation ANALOGUE_PREV = CUtil.rl("analogue_prev");
    ResourceLocation DIGITAL_NOW = CUtil.rl("digital_now");
    ResourceLocation DIGITAL_PREV = CUtil.rl("digital_prev");
    ResourceLocation JUST_PRESSED = CUtil.rl("just_pressed");
    ResourceLocation JUST_RELEASED = CUtil.rl("just_released");
    ResourceLocation JUST_TAPPED = CUtil.rl("just_tapped");
    ResourceLocation GUI_PRESSED = CUtil.rl("gui_pressed");
    ResourceLocation KEY_EMULATION = CUtil.rl("key_emulation");

    <T extends DigitalOutput> T getDigitalOutput(ResourceLocation id);
    <T extends DigitalOutput> T addDigitalOutput(ResourceLocation id, T output);

    <T extends AnalogueOutput> T getAnalogueOutput(ResourceLocation id);
    <T extends AnalogueOutput> T addAnalogueOutput(ResourceLocation id, T output);
}
