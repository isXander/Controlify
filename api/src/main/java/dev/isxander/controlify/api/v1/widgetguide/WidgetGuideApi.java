package dev.isxander.controlify.api.v1.widgetguide;

import dev.isxander.controlify.api.v1.bindings.InputBindingSupplier;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public interface WidgetGuideApi {
    /**
     * Register a visual guide for a widget for the binding.
     * <p>
     * For example, applying a button guide to a button will add the glyph of the bound input
     * of the binding next to the text of the button.
     * <p>
     * This is purely a guide, and does not add any actual input handling to the widget.
     * This should be handled separately via other means.
     * <p>
     * Mods can allow their custom widgets to be guide-capable by implementing
     * {@link WidgetGuideCapable}. The reason <code>T</code> is unbounded is to allow
     * for mixins to implement the interface on behalf of the widget class without interface injection.
     * @param widget the widget to register the guide for
     * @param bindingSupplier the binding to show the guide for. the supplier can change each frame depending on context
     * @param renderPredicate predicate to determine whether the guide should be rendered
     * @param <T> the type of the widget
     */
    <T> void registerGuide(
            @NonNull T widget,
            @NonNull Supplier<InputBindingSupplier> bindingSupplier,
            @NonNull WidgetGuidePredicate<T> renderPredicate
    );

    /**
     * Register a visual guide for a widget for the binding.
     * <p>
     * For example, applying a button guide to a button will add the glyph of the bound input
     * of the binding next to the text of the button.
     * <p>
     * This is purely a guide, and does not add any actual input handling to the widget.
     * This should be handled separately via other means.
     * <p>
     * Mods can allow their custom widgets to be guide-capable by implementing
     * {@link WidgetGuideCapable}. The reason <code>T</code> is unbounded is to allow
     * for mixins to implement the interface on behalf of the widget class without interface injection.
     * @param widget the widget to register the guide for
     * @param binding the binding to show the guide for.
     * @param renderPredicate predicate to determine whether the guide should be rendered
     * @param <T> the type of the widget
     */
    default <T> void registerGuide(
            @NonNull T widget,
            @NonNull InputBindingSupplier binding,
            @NonNull WidgetGuidePredicate<T> renderPredicate
    ) {
        this.registerGuide(widget, () -> binding, renderPredicate);
    }
}
