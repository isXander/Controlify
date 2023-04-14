package dev.isxander.controlify.gui;

import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import dev.isxander.controlify.bindings.ControllerBindings;
import net.minecraft.client.gui.components.AbstractButton;

import java.util.function.Function;

/**
 * @see dev.isxander.controlify.mixins.feature.guide.screen.AbstractButtonMixin
 */
public interface ButtonGuideRenderer<T extends AbstractButton> {
    void setButtonGuide(RenderData<T> renderData);

    static <T extends AbstractButton> void registerBindingForButton(T button, Function<ControllerBindings<?>, ControllerBinding> binding, ButtonRenderPosition position, ButtonGuidePredicate<T> renderPredicate) {
        ((ButtonGuideRenderer<T>) button).setButtonGuide(new RenderData<>(binding, position, renderPredicate));
    }

    record RenderData<T extends AbstractButton>(Function<ControllerBindings<?>, ControllerBinding> binding, ButtonRenderPosition position, ButtonGuidePredicate<T> renderPredicate) {
    }
}
