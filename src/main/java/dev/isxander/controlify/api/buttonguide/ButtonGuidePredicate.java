package dev.isxander.controlify.api.buttonguide;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;

@FunctionalInterface
public interface ButtonGuidePredicate<T> {
    boolean shouldDisplay(T button);

    static <T extends AbstractWidget> ButtonGuidePredicate<T> focusOnly() {
        return AbstractWidget::isFocused;
    }

    /** Always display the button guide. */
    static <T> ButtonGuidePredicate<T> always() {
        return btn -> true;
    }

    /** Always display the button guide. */
    @Deprecated
    ButtonGuidePredicate<AbstractButton> ALWAYS = btn -> true;

    /** Only display the button guide when the button is focused. */
    @Deprecated
    ButtonGuidePredicate<AbstractButton> FOCUS_ONLY = AbstractWidget::isFocused;
}
