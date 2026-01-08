package dev.isxander.controlify.apiimpl.v1;

import dev.isxander.controlify.api.v1.bindings.InputBindingSupplier;
import dev.isxander.controlify.api.v1.widgetguide.WidgetGuideApi;
import dev.isxander.controlify.api.v1.widgetguide.WidgetGuideCapable;
import dev.isxander.controlify.api.v1.widgetguide.WidgetGuideConfig;
import dev.isxander.controlify.api.v1.widgetguide.WidgetGuidePredicate;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

class WidgetGuideApiImpl implements WidgetGuideApi {
    static final WidgetGuideApiImpl INSTANCE = new WidgetGuideApiImpl();

    private WidgetGuideApiImpl() {
    }

    @Override
    public <T> void registerGuide(
            @NonNull T widget,
            @NonNull Supplier<InputBindingSupplier> bindingSupplier,
            @NonNull WidgetGuidePredicate<T> renderPredicate
    ) {
        if (widget instanceof WidgetGuideCapable<?> guideCapable) {
            ((WidgetGuideCapable<T>) guideCapable).controlify$acceptWidgetGuideConfig(
                    new WidgetGuideConfig<>(bindingSupplier, renderPredicate)
            );
        } else {
            ButtonGuideRenderer.registerBindingForButton(
                    widget,
                    () -> ((InputBindingSupplierImpl) bindingSupplier.get()).impl(),
                    renderPredicate::shouldDisplay
            );
        }
    }
}
