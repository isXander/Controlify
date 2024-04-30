package dev.isxander.controlify.gui;

import dev.isxander.controlify.api.bind.BindingSupplier;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import net.minecraft.client.gui.components.AbstractButton;

/**
 * @see dev.isxander.controlify.mixins.feature.guide.screen.AbstractButtonMixin
 */
public interface ButtonGuideRenderer<T extends AbstractButton> {
    void controlify$setButtonGuide(RenderData<T> renderData);

    static <T extends AbstractButton> void registerBindingForButton(T button, BindingSupplier binding, ButtonGuidePredicate<T> renderPredicate) {
        ((ButtonGuideRenderer<T>) button).controlify$setButtonGuide(new RenderData<>(binding, renderPredicate));
    }

    record RenderData<T extends AbstractButton>(BindingSupplier binding, ButtonGuidePredicate<T> renderPredicate) {
    }
}
