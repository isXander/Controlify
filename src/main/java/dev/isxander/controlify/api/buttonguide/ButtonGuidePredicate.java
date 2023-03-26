package dev.isxander.controlify.api.buttonguide;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface ButtonGuidePredicate<T extends AbstractButton> {
    boolean shouldDisplay(T button);

    ButtonGuidePredicate<AbstractButton> FOCUS_ONLY = AbstractWidget::isFocused;
    ButtonGuidePredicate<AbstractButton> ALWAYS = btn -> true;
}
