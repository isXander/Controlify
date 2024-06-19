package dev.isxander.controlify.gui;

import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Supplier;

/**
 * @see dev.isxander.controlify.mixins.feature.guide.screen.AbstractButtonMixin
 */
public interface ButtonGuideRenderer<T extends AbstractWidget> {
    void controlify$setButtonGuide(RenderData<T> renderData);

    static <T extends AbstractButton> void registerBindingForButton(T button, Supplier<InputBindingSupplier> binding, ButtonGuidePredicate<T> renderPredicate) {
        ((ButtonGuideRenderer<T>) button).controlify$setButtonGuide(new RenderData<>(binding, renderPredicate));
    }

    record RenderData<T extends AbstractWidget>(Supplier<InputBindingSupplier> binding, ButtonGuidePredicate<T> renderPredicate) {
    }
}
