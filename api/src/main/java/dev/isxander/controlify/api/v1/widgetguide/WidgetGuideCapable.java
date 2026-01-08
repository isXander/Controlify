package dev.isxander.controlify.api.v1.widgetguide;

/**
 * An interface for widgets that can accept widget guide configurations.
 * It is up to the widget to do something meaningful with the provided configuration.
 * @param <T> The type of the widget implementing this interface.
 */
public interface WidgetGuideCapable<T> {
    /**
     * Accepts a widget guide configuration.
     * This is called when registered with {@link WidgetGuideApi#registerGuide}.
     * @param config The widget guide configuration to accept.
     */
    void controlify$acceptWidgetGuideConfig(WidgetGuideConfig<T> config);
}
