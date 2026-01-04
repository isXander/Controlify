package dev.isxander.controlify.api.v1.widgetguide;

public interface WidgetGuideCapable<T extends WidgetGuideCapable<T>> {
    void controlify$acceptWidgetGuideConfig(WidgetGuideConfig<T> config);
}
