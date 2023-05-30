package dev.isxander.controlify.gui.controllers;

import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.EmptyBind;
import dev.isxander.controlify.bindings.IBind;
import dev.isxander.controlify.controller.ControllerState;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.yacl.api.Controller;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.utils.Dimension;
import dev.isxander.yacl.gui.YACLScreen;
import dev.isxander.yacl.gui.controllers.ControllerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public abstract class AbstractBindController<T extends ControllerState> implements Controller<IBind<T>> {
    private final Option<IBind<T>> option;
    public final dev.isxander.controlify.controller.Controller<T, ?> controller;
    private boolean conflicting;

    public AbstractBindController(Option<IBind<T>> option, dev.isxander.controlify.controller.Controller<T, ?> controller) {
        this.option = option;
        this.controller = controller;
    }

    @Override
    public Option<IBind<T>> option() {
        return this.option;
    }

    @Override
    public Component formatValue() {
        return Component.empty();
    }

    public void setConflicting(boolean conflicting) {
        this.conflicting = conflicting;
    }

    public boolean getConflicting() {
        return this.conflicting;
    }

    @Override
    public abstract AbstractBindControllerElement<T> provideWidget(YACLScreen yaclScreen, Dimension<Integer> dimension);

    public abstract static class AbstractBindControllerElement<T extends ControllerState> extends ControllerWidget<AbstractBindController<T>> implements ComponentProcessor, ControlifyEvents.ControllerStateUpdate {
        private boolean awaitingControllerInput = false;
        private boolean justTookInput = false;
        private final Component awaitingText = Component.translatable("controlify.gui.bind_input_awaiting").withStyle(ChatFormatting.ITALIC);

        public AbstractBindControllerElement(AbstractBindController<T> control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        protected void drawValueText(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            if (awaitingControllerInput) {
                graphics.drawString(textRenderer, awaitingText, getDimension().xLimit() - textRenderer.width(awaitingText) - getXPadding(), (int)(getDimension().centerY() - textRenderer.lineHeight / 2f), 0xFFFFFF, true);
            } else {
                var bind = control.option().pendingValue();
                bind.draw(graphics, getDimension().xLimit() - bind.drawSize().width(), getDimension().centerY());
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

            if (!justTookInput && !awaitingControllerInput) {
                if (controller.bindings().CLEAR_BINDING.justPressed()) {
                    control.option().requestSet(new EmptyBind<>());
                    return true;
                }

                if (controller.bindings().GUI_PRESS.justPressed()) {
                    ControllerBindHandler.setBindListener(this);
                    return awaitingControllerInput = true;
                }
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

            Optional<IBind<T>> pressedBind = getPressedBind();
            if (pressedBind.isPresent()) {
                control.option().requestSet(pressedBind.get());
                awaitingControllerInput = false;
                justTookInput = true;
                ControllerBindHandler.clearBindListener();
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

        @Override
        protected int getValueColor() {
            return control.conflicting ? 0xFF5555 : super.getValueColor();
        }

        public abstract Optional<IBind<T>> getPressedBind();
    }
}
