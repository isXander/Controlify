package dev.isxander.controlify.api.v1.widgetguide;


@FunctionalInterface
public interface WidgetGuidePredicate<T> {
    boolean shouldDisplay(T button);

    /** Always display the button guide. */
    static <T> WidgetGuidePredicate<T> always() {
        return btn -> true;
    }

}
