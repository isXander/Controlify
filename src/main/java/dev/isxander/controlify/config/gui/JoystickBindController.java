package dev.isxander.controlify.config.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.bindings.*;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.yacl.api.Controller;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.utils.Dimension;
import dev.isxander.yacl.gui.AbstractWidget;
import dev.isxander.yacl.gui.YACLScreen;
import dev.isxander.yacl.gui.controllers.ControllerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class JoystickBindController implements Controller<IBind<JoystickState>> {
    private final Option<IBind<JoystickState>> option;
    private final JoystickController controller;

    public JoystickBindController(Option<IBind<JoystickState>> option, JoystickController controller) {
        this.option = option;
        this.controller = controller;
    }

    @Override
    public Option<IBind<JoystickState>> option() {
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

    public static class BindButtonWidget extends ControllerWidget<JoystickBindController> implements ComponentProcessor {
        private boolean awaitingControllerInput = false;
        private final Component awaitingText = Component.translatable("controlify.gui.bind_input_awaiting").withStyle(ChatFormatting.ITALIC);

        public BindButtonWidget(JoystickBindController control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        protected void drawValueText(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (awaitingControllerInput) {
                textRenderer.drawShadow(matrices, awaitingText, getDimension().xLimit() - textRenderer.width(awaitingText) - getXPadding(), getDimension().centerY() - textRenderer.lineHeight / 2f, 0xFFFFFF);
            } else {
                var bind = control.option().pendingValue();
                bind.draw(matrices, getDimension().xLimit() - bind.drawSize().width(), getDimension().centerY());
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
        public boolean overrideControllerButtons(ScreenProcessor<?> screen, dev.isxander.controlify.controller.Controller<?, ?> controller) {
            if (controller != control.controller) return true;

            if (controller.bindings().GUI_PRESS.justPressed() && !awaitingControllerInput) {
                return awaitingControllerInput = true;
            }

            if (!awaitingControllerInput) return false;

            var joystick = control.controller;

            var state = joystick.state();
            var prevState = joystick.prevState();

            for (int i = 0; i < Math.min(state.buttons().size(), prevState.buttons().size()); i++) {
                if (state.buttons().get(i) && !prevState.buttons().get(i)) {
                    control.option().requestSet(new JoystickButtonBind(joystick, i));
                    awaitingControllerInput = false;
                    return true;
                }
            }

            for (int i = 0; i < Math.min(state.axes().size(), prevState.axes().size()); i++) {
                var axis = state.axes().get(i);
                var prevAxis = prevState.axes().get(i);
                var activationThreshold = joystick.config().buttonActivationThreshold;

                if (Math.abs(prevAxis) < activationThreshold) {
                    if (axis > activationThreshold) {
                        control.option().requestSet(new JoystickAxisBind(joystick, i, JoystickAxisBind.AxisDirection.POSITIVE));
                        awaitingControllerInput = false;
                        return true;
                    } else if (axis < -activationThreshold) {
                        control.option().requestSet(new JoystickAxisBind(joystick, i, JoystickAxisBind.AxisDirection.NEGATIVE));
                        awaitingControllerInput = false;
                        return true;
                    }
                }
            }

            for (int i = 0; i < Math.min(state.hats().size(), prevState.hats().size()); i++) {
                var hat = state.hats().get(i);
                var prevHat = prevState.hats().get(i);

                if (prevHat.isCentered() && !hat.isCentered()) {
                    control.option().requestSet(new JoystickHatBind(joystick, i, hat));
                    awaitingControllerInput = false;
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean overrideControllerNavigation(ScreenProcessor<?> screen, dev.isxander.controlify.controller.Controller<?, ?> controller) {
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
