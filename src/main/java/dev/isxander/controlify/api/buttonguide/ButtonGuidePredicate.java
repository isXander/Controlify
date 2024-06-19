package dev.isxander.controlify.api.buttonguide;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;

@FunctionalInterface
public interface ButtonGuidePredicate<T extends AbstractWidget> {
    boolean shouldDisplay(T button);

    /** Only display the button guide when the button is focused. */
    ButtonGuidePredicate<AbstractButton> FOCUS_ONLY = AbstractWidget::isFocused;
    /** Always display the button guide. */
    ButtonGuidePredicate<AbstractButton> ALWAYS = btn -> true;
}
