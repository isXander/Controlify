package dev.isxander.controlify.api.buttonguide;

import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;

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
    public static <T> void addGuideToButton(
            T button,
            InputBindingSupplier binding,
            ButtonGuidePredicate<T> renderPredicate) {
        ButtonGuideRenderer.registerBindingForButton(button, () -> binding, renderPredicate);
    }

    public static <T> void addGuideToButton(
            T button,
            Supplier<InputBindingSupplier> binding,
            ButtonGuidePredicate<T> renderPredicate) {
        ButtonGuideRenderer.registerBindingForButton(button, binding, renderPredicate);
    }
}

