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
import org.jetbrains.annotations.ApiStatus;

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
    /**
     * The ID of the binding, set from {@link InputBindingBuilder#id(ResourceLocation)}.
     * @return the ID
     */
    ResourceLocation id();

    /**
     * The name of the binding.
     * @return the name
     */
    Component name();

    /**
     * The description of the binding.
     * @return the description
     */
    Component description();

    /**
     * The category of the binding.
     * @return the category
     */
    Component category();

    /**
     * The icon glyph of the input
     * @return the icon component
     */
    Component inputIcon();

    /**
     * Create a new state access with the given history required.
     * Please use the minimum amount of history required.
     *
     * @param historyRequired the amount of ticks of history required.
     * @return the state access
     */
    StateAccess createStateAccess(int historyRequired);

    /**
     * Create a new state access with the given history required and push event.
     * Please use the minimum amount of history required.
     *
     * @param historyRequired the amount of ticks of history required.
     * @param pushEvent the event to call when a new state is pushed.
     * @return the state access
     */
    StateAccess createStateAccess(int historyRequired, Consumer<StateAccess> pushEvent);

    /**
     * Returns the state access created with {@link #createStateAccess(int)}.
     * This renders it unusable forever.
     * This frees up history if possible.
     *
     * @param stateAccess the state access to return
     */
    void returnStateAccess(StateAccess stateAccess);

    @ApiStatus.Internal
    void pushState(ControllerStateView state);

    /**
     * Emulates a tap of this binding.
     * This is used by the radial menu.
     */
    void fakePress();

    /**
     * Set the input that this binding is bound to.
     *
     * @param input input to bind
     */
    void setBoundInput(Input input);

    /**
     * Get the input that this binding is bound to.
     *
     * @return currently bound input
     */
    Input boundInput();

    /**
     * Get the default input for this binding.
     * This can change if the user reloads resources and the default changes
     * (since it's data-driven).
     *
     * @return default input
     */
    Input defaultInput();

    /**
     * @return true if the binding is equal to {@link EmptyInput}
     */
    default boolean isUnbound() {
        return EmptyInput.equals(this.boundInput());
    }

    /**
     * Gets all contexts associated with this binding,
     * not just the currently applicable ones.
     * This is set from {@link InputBindingBuilder#allowedContexts(BindContext...)}.
     *
     * @return the contexts
     */
    Set<BindContext> contexts();

    /**
     * Returns the radial icon's ID.
     * If the binding does not have a radial icon, this will return an empty optional.
     * If empty, consider this binding as not being a radial candidate. It can never be added to the radial menu.
     * @return the radial icon's ID or an empty optional
     */
    Optional<ResourceLocation> radialIcon();

    /**
     * Equivalent to calling
     * <pre><code>
     *     StateAccess stateAccess = binding.createStateAccess(0);
     *     float analogueNow = stateAccess.analogue(0);
     * </code></pre>
     *
     * @return the current analogue state (0-1, never negative),
     * this tick.
     */
    float analogueNow();

    /**
     * Equivalent to calling
     * <pre><code>
     *     StateAccess stateAccess = binding.createStateAccess(1);
     *     float analoguePrev = stateAccess.analogue(1);
     * </code></pre>
     *
     * @return the previous analogue state (0-1, never negative), 1 tick ago
     */
    float analoguePrev();

    /**
     * Equivalent to calling
     * <pre><code>
     *     StateAccess stateAccess = binding.createStateAccess(0);
     *     boolean digitalNow = stateAccess.digital(0);
     * </code></pre>
     *
     * @return the current digital state (true or false), this tick
     */
    boolean digitalNow();

    /**
     * Equivalent to calling
     * <pre><code>
     *     StateAccess stateAccess = binding.createStateAccess(1);
     *     boolean digitalPrev = stateAccess.digital(1);
     * </code></pre>
     *
     * @return the previous digital state (true or false), 1 tick ago
     */
    boolean digitalPrev();

    /**
     * Equivalent to calling
     * <pre><code>
     *     binding.digitalNow() && !binding.digitalPrev()
     * </code></pre>
     *
     * @return true if the binding is pressed this tick and not pressed the previous tick
     */
    boolean justPressed();

    /**
     * Equivalent to calling
     * <pre><code>
     *     !binding.digitalNow() && binding.digitalPrev()
     * </code></pre>
     *
     * @return true if the binding is not pressed this tick and pressed the previous tick
     */
    boolean justReleased();

    /**
     * @return true if the binding got pressed, and then released
     */
    boolean justTapped();

    /**
     * A more advanced output that returns true after the input was released,
     * but only if the player has not navigated away from the button where the input was initially pressed.
     *
     * @return the output
     */
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
