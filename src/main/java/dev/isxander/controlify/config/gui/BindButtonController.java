package dev.isxander.controlify.config.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.bindings.Bind;
import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessorProvider;
import dev.isxander.controlify.event.ControlifyEvents;
import dev.isxander.controlify.gui.ButtonRenderer;
import dev.isxander.yacl.api.Controller;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.utils.Dimension;
import dev.isxander.yacl.gui.AbstractWidget;
import dev.isxander.yacl.gui.YACLScreen;
import dev.isxander.yacl.gui.controllers.ControllerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class BindButtonController implements Controller<Bind> {
    private final Option<Bind> option;
    private final dev.isxander.controlify.controller.Controller controller;

    public BindButtonController(Option<Bind> option, dev.isxander.controlify.controller.Controller controller) {
        this.option = option;
        this.controller = controller;
    }

    @Override
    public Option<Bind> option() {
        return this.option;
    }

    @Override
    public Component formatValue() {
        return Component.literal(option().pendingValue().identifier());
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen yaclScreen, Dimension<Integer> dimension) {
        return new BindButtonWidget(this, yaclScreen, dimension);
    }

    public static class BindButtonWidget extends ControllerWidget<BindButtonController> implements ComponentProcessorProvider, ComponentProcessor {
        private boolean awaitingControllerInput = false;
        private final Component awaitingText = Component.translatable("controlify.gui.bind_input_awaiting").withStyle(ChatFormatting.ITALIC);

        public BindButtonWidget(BindButtonController control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        protected void drawValueText(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (awaitingControllerInput) {
                textRenderer.drawShadow(matrices, awaitingText, getDimension().xLimit() - textRenderer.width(awaitingText) - getXPadding(), getDimension().centerY() - textRenderer.lineHeight / 2f, 0xFFFFFF);
            } else {
                ButtonRenderer.drawButton(control.option().pendingValue(), control.controller, matrices, getDimension().xLimit() - ButtonRenderer.BUTTON_SIZE / 2, getDimension().centerY(), ButtonRenderer.BUTTON_SIZE);
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (isFocused() && keyCode == GLFW.GLFW_KEY_ENTER && !awaitingControllerInput) {
                awaitingControllerInput = true;
                return true;
            }

            return false;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (getDimension().isPointInside((int)mouseX, (int)mouseY)) {
                awaitingControllerInput = true;
                return true;
            }

            return false;
        }

        @Override
        public ComponentProcessor componentProcessor() {
            return this;
        }

        @Override
        public boolean overrideControllerButtons(ScreenProcessor screen, dev.isxander.controlify.controller.Controller controller) {
            if (!awaitingControllerInput || !isFocused()) return false;

            for (var bind : Bind.values()) {
                boolean stateNow = bind.state(controller.state(), controller);
                boolean stateBefore = bind.state(controller.prevState(), controller);
                if (stateNow && !stateBefore) {
                    control.option().requestSet(bind);
                    awaitingControllerInput = false;
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean overrideControllerNavigation(ScreenProcessor screen, dev.isxander.controlify.controller.Controller controller) {
            return awaitingControllerInput;
        }

        @Override
        protected int getHoveredControlWidth() {
            return getUnhoveredControlWidth();
        }

        @Override
        protected int getUnhoveredControlWidth() {
            if (awaitingControllerInput)
                return textRenderer.width(awaitingText);

            return ButtonRenderer.BUTTON_SIZE;
        }
    }
}
