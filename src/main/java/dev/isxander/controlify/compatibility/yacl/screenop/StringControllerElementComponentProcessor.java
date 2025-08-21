package dev.isxander.controlify.compatibility.yacl.screenop;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.compatibility.yacl.mixins.StringControllerElementAccessor;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.keyboard.ComponentKeyboardBehaviour;
import dev.isxander.controlify.screenop.keyboard.InputTarget;
import dev.isxander.controlify.screenop.keyboard.KeyboardLayouts;
import dev.isxander.controlify.screenop.keyboard.KeyboardOverlayScreen;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;

public class StringControllerElementComponentProcessor implements ComponentProcessor {
    private final StringControllerElement element;

    public StringControllerElementComponentProcessor(StringControllerElement element) {
        this.element = element;
    }

    @Override
    public ComponentKeyboardBehaviour getKeyboardBehaviour(ScreenProcessor<?> screen, ControllerEntity controller) {
        return new ComponentKeyboardBehaviour.Handled(
                KeyboardLayouts.simple(),
                new StringInputTarget(),
                KeyboardOverlayScreen.aboveOrBelowWidgetPositioner(
                        (int) (screen.screen.width * 0.8f), (int) (screen.screen.height * 0.4f),
                        1,
                        element::getRectangle
                )
        );
    }

    private class StringInputTarget implements InputTarget {
        @Override
        public boolean supportsCharInput() {
            return true;
        }

        @Override
        public boolean acceptChar(char ch, int modifiers) {
            return element.charTyped(ch, modifiers);
        }

        @Override
        public boolean supportsKeyCodeInput() {
            return true;
        }

        @Override
        public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
            return element.keyPressed(keycode, scancode, modifiers);
        }

        @Override
        public boolean supportsCursorMovement() {
            return true;
        }

        @Override
        public boolean moveCursor(int amount) {
            int keycode = amount > 0 ? InputConstants.KEY_RIGHT : InputConstants.KEY_LEFT;
            for (int i = 0; i < amount; i++) {
                element.keyPressed(keycode, 0, 0);
            }
            return true;
        }

        @Override
        public boolean supportsCopying() {
            return true;
        }

        @Override
        public boolean copy() {
            return ((StringControllerElementAccessor) element).callDoCopy();
        }
    }
}
