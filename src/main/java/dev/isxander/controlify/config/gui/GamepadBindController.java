package dev.isxander.controlify.config.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.GamepadBind;
import dev.isxander.controlify.bindings.GamepadBinds;
import dev.isxander.controlify.bindings.IBind;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.yacl.api.Controller;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.utils.Dimension;
import dev.isxander.yacl.gui.AbstractWidget;
import dev.isxander.yacl.gui.YACLScreen;
import dev.isxander.yacl.gui.controllers.ControllerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GamepadBindController implements Controller<IBind<GamepadState>> {
    private final Option<IBind<GamepadState>> option;
    private final GamepadController controller;

    public GamepadBindController(Option<IBind<GamepadState>> option, GamepadController controller) {
        this.option = option;
        this.controller = controller;
    }

    @Override
    public Option<IBind<GamepadState>> option() {
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

    public static class BindButtonWidget extends ControllerWidget<GamepadBindController> implements ComponentProcessor, ControlifyEvents.ControllerStateUpdate {
        private boolean awaitingControllerInput = false;
        private boolean justTookInput = false;
        private final Component awaitingText = Component.translatable("controlify.gui.bind_input_awaiting").withStyle(ChatFormatting.ITALIC);

        public BindButtonWidget(GamepadBindController control, YACLScreen screen, Dimension<Integer> dim) {
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
                ControllerBindHandler.setBindListener(this);
                return true;
            }

            return false;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (getDimension().isPointInside((int)mouseX, (int)mouseY)) {
                awaitingControllerInput = true;
                ControllerBindHandler.setBindListener(this);
                return true;
            }

            return false;
        }

        @Override
        public boolean overrideControllerButtons(ScreenProcessor<?> screen, dev.isxander.controlify.controller.Controller<?, ?> controller) {
            if (controller != control.controller) return true;

            if (controller.bindings().GUI_PRESS.justPressed() && !awaitingControllerInput) {
                ControllerBindHandler.setBindListener(this);
                return awaitingControllerInput = true;
            }

            if (justTookInput) {
                justTookInput = false;
                return true;
            }

            return false;
        }

        @Override
        public void onControllerStateUpdate(dev.isxander.controlify.controller.Controller<?, ?> controller) {
            if (controller != control.controller) return;

            if (!awaitingControllerInput) return;

            var gamepad = control.controller;

            for (var bindType : GamepadBinds.values()) {
                GamepadBind bind = bindType.forGamepad(gamepad);
                if (bind.held(gamepad.state()) && !bind.held(gamepad.prevState())) {
                    control.option().requestSet(bind);
                    awaitingControllerInput = false;
                    justTookInput = true;
                    ControllerBindHandler.clearBindListener();
                    return;
                }
            }
        }

        @Override
        public boolean overrideControllerNavigation(ScreenProcessor<?> screen, dev.isxander.controlify.controller.Controller<?, ?> controller) {
            return awaitingControllerInput || justTookInput;
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
