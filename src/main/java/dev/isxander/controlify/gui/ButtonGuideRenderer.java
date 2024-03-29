package dev.isxander.controlify.gui;

import dev.isxander.controlify.api.bind.BindingSupplier;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import net.minecraft.client.gui.components.AbstractButton;

/**
 * @see dev.isxander.controlify.mixins.feature.guide.screen.AbstractButtonMixin
 */
public interface ButtonGuideRenderer<T extends AbstractButton> {
    void setButtonGuide(RenderData<T> renderData);

    static <T extends AbstractButton> void registerBindingForButton(T button, BindingSupplier binding, ButtonRenderPosition position, ButtonGuidePredicate<T> renderPredicate) {
        ((ButtonGuideRenderer<T>) button).setButtonGuide(new RenderData<>(binding, position, renderPredicate));
    }

    record RenderData<T extends AbstractButton>(BindingSupplier binding, ButtonRenderPosition position, ButtonGuidePredicate<T> renderPredicate) {
    }
}
