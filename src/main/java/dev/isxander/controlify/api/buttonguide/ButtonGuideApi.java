package dev.isxander.controlify.api.buttonguide;

import dev.isxander.controlify.api.bind.BindingSupplier;
import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Function;

/**
 * Adds a guide to a button. This does not invoke the button press on binding trigger, only renders the guide.
 * This should be called every time a button is initialised, like in {@link Screen#init()}
 */
public final class ButtonGuideApi {
    /**
     * Makes the button render the image of the binding specified.
     * This does not invoke the button press on binding trigger, only renders the guide.
     * Custom behaviour should be handled inside a {@link dev.isxander.controlify.screenop.ScreenProcessor} or {@link dev.isxander.controlify.screenop.ComponentProcessor}
     *
     * @param button          button to render the guide for
     * @param binding         the custom binding to render
     * @param renderPredicate whether the guide should be rendered
     */
    public static <T extends AbstractButton> void addGuideToButton(
            T button,
            BindingSupplier binding,
            ButtonGuidePredicate<T> renderPredicate) {
        ButtonGuideRenderer.registerBindingForButton(button, binding, renderPredicate);
    }

    /**
     * Makes the button render the image of the binding specified.
     * This does not invoke the button press on binding trigger, only renders the guide.
     * Custom behaviour should be handled inside a {@link dev.isxander.controlify.screenop.ScreenProcessor} or {@link dev.isxander.controlify.screenop.ComponentProcessor}
     *
     * @param button          button to render the guide for
     * @param binding         gets the binding to render
     * @param renderPredicate whether the guide should be rendered
     */
    public static <T extends AbstractButton> void addGuideToButtonBuiltin(
            T button,
            Function<ControllerBindings, ControllerBinding> binding,
            ButtonGuidePredicate<T> renderPredicate) {
        ButtonGuideRenderer.registerBindingForButton(button, controller -> binding.apply(controller.bindings()), renderPredicate);
    }

    @Deprecated
    public static <T extends AbstractButton> void addGuideToButton(
            T button,
            BindingSupplier binding,
            ButtonRenderPosition position,
            ButtonGuidePredicate<T> renderPredicate) {
        ButtonGuideApi.addGuideToButton(button, binding, renderPredicate);
    }

    @Deprecated
    public static <T extends AbstractButton> void addGuideToButtonBuiltin(
            T button,
            Function<ControllerBindings, ControllerBinding> binding,
            ButtonRenderPosition position,
            ButtonGuidePredicate<T> renderPredicate) {
        ButtonGuideApi.addGuideToButtonBuiltin(button, binding, renderPredicate);
    }
}

