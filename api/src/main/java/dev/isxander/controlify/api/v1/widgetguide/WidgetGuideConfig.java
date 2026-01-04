package dev.isxander.controlify.api.v1.widgetguide;

import dev.isxander.controlify.api.v1.bindings.InputBindingSupplier;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public record WidgetGuideConfig<T>(
        @NonNull Supplier<InputBindingSupplier> bindingSupplier,
        @NonNull WidgetGuidePredicate<T> renderPredicate
) {}
