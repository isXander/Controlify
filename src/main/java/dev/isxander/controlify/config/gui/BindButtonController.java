package dev.isxander.controlify.config.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.bindings.Bind;
import dev.isxander.controlify.bindings.IBind;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.yacl.api.Controller;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.utils.Dimension;
import dev.isxander.yacl.gui.AbstractWidget;
import dev.isxander.yacl.gui.YACLScreen;
import dev.isxander.yacl.gui.controllers.ControllerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashSet;
import java.util.Set;

public class BindButtonController implements Controller<IBind> {
    private final Option<IBind> option;
    private final dev.isxander.controlify.controller.Controller controller;

    public BindButtonController(Option<IBind> option, dev.isxander.controlify.controller.Controller controller) {
        this.option = option;
        this.controller = controller;
    }

    @Override
    public Option<IBind> option() {
        return this.option;
    }

    @Override
    public Component formatValue() {
        return Component.empty();
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen yaclScreen, Dimension<Integer> dimension) {
        return new BindButtonWidget(this, yaclScreen, dimension);
    }

    public static class BindButtonWidget extends ControllerWidget<BindButtonController> implements ComponentProcessorProvider, ComponentProcessor {
        private boolean awaitingControllerInput = false;
        private final Component awaitingText = Component.translatable("controlify.gui.bind_input_awaiting").withStyle(ChatFormatting.ITALIC);
        private final Set<Bind> pressedBinds = new LinkedHashSet<>();

        public BindButtonWidget(BindButtonController control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        protected void drawValueText(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (awaitingControllerInput) {
                if (pressedBinds.isEmpty()) {
                    textRenderer.drawShadow(matrices, awaitingText, getDimension().xLimit() - textRenderer.width(awaitingText) - getXPadding(), getDimension().centerY() - textRenderer.lineHeight / 2f, 0xFFFFFF);
                } else {
                    var bind = IBind.create(pressedBinds);
                    var plusSize = 2 + textRenderer.width("+");
                    bind.draw(matrices, getDimension().xLimit() - bind.drawSize().width() - getXPadding() - plusSize, getDimension().centerY(), control.controller);
                    textRenderer.drawShadow(matrices, "+", getDimension().xLimit() - getXPadding() - plusSize, getDimension().centerY() - textRenderer.lineHeight / 2f, 0xFFFFFF);
                }
            } else {
                var bind = control.option().pendingValue();
                bind.draw(matrices, getDimension().xLimit() - bind.drawSize().width(), getDimension().centerY(), control.controller);
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
        public boolean overrideControllerButtons(ScreenProcessor<?> screen, dev.isxander.controlify.controller.Controller controller) {
            if (controller.bindings().GUI_PRESS.justPressed() && !awaitingControllerInput) {
                return awaitingControllerInput = true;
            }

            if (!awaitingControllerInput) return false;

            if (pressedBinds.stream().anyMatch(bind -> !bind.held(controller.state(), controller))) {
                // finished
                awaitingControllerInput = false;
                control.option().requestSet(IBind.create(pressedBinds));
                pressedBinds.clear();
            } else {
                for (var bind : Bind.values()) {
                    if (bind.held(controller.state(), controller) && !bind.held(controller.prevState(), controller)) {
                        if (bind == Bind.GUIDE) { // FIXME: guide cannot be used as reserve because Windows hooks into xbox button to open game bar, maybe START?
                            if (pressedBinds.isEmpty()) {
                                awaitingControllerInput = false;
                                control.option().requestSet(IBind.create(Bind.NONE));
                                pressedBinds.clear();
                                return true;
                            }
                        } else {
                            pressedBinds.add(bind);
                        }
                    }
                }
                control.controller.consumeButtonState();
            }

            return true;
        }

        @Override
        public boolean overrideControllerNavigation(ScreenProcessor<?> screen, dev.isxander.controlify.controller.Controller controller) {
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

            return control.option().pendingValue().drawSize().width();
        }
    }
}
