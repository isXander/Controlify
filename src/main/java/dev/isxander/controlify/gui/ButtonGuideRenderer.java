package dev.isxander.controlify.gui;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @see dev.isxander.controlify.mixins.feature.guide.screen.AbstractWidgetMixin
 */
public interface ButtonGuideRenderer<T> {
    void controlify$setButtonGuide(RenderData<T> renderData);

    static <T> void registerBindingForButton(T button, Supplier<InputBindingSupplier> binding, ButtonGuidePredicate<T> renderPredicate) {
        ((ButtonGuideRenderer<T>) button).controlify$setButtonGuide(new RenderData<>(binding, renderPredicate));
    }

    record RenderData<T>(Supplier<InputBindingSupplier> binding, ButtonGuidePredicate<T> renderPredicate) {
        public Component getControllerMessage(InputBinding bind, Component actualLabel) {
            var component = Component.empty();
            if (!Minecraft.getInstance().font.isBidirectional()) {
                component.append(bind.inputIcon());
                component.append(CommonComponents.SPACE);
            }
            component.append(actualLabel);
            if (Minecraft.getInstance().font.isBidirectional()) {
                component.append(CommonComponents.SPACE);
                component.append(bind.inputIcon());
            }
            return component;
        }

        public boolean shouldRender(T renderable) {
            Optional<InputBinding> binding = getBind();
            return binding.isPresent()
                    && Controlify.instance().currentInputMode().isController()
                    && Controlify.instance().getCurrentController().map(c -> c.genericConfig().config().showScreenGuides).orElse(false)
                    && !binding.get().isUnbound()
                    && renderPredicate().shouldDisplay(renderable);
        }

        public Optional<InputBinding> getBind() {
            return Controlify.instance().getCurrentController().map(c -> binding().get().on(c));
        }
    }
}
