package dev.isxander.controlify.screenop.compat.vanilla;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.keyboard.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;

public class EditBoxComponentProcessor implements ComponentProcessor {

    private final EditBox editBox;
    private final int screenWidth, screenHeight;
    private ComponentKeyboardBehaviour keyboardBehaviour;

    private KeyboardLayoutWithId keyboardLayout;
    private InputTarget inputTarget;
    private KeyboardOverlayScreen.KeyboardPositioner positioner;

    public EditBoxComponentProcessor(EditBox editBox, int screenWidth, int screenHeight) {
        this.editBox = editBox;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.keyboardLayout = KeyboardLayouts.simple();
        this.inputTarget = new EditBoxInputTarget(editBox);
        this.positioner = KeyboardOverlayScreen.aboveOrBelowWidgetPositioner(
                (int) (screenWidth * 0.8f), (int) (screenHeight * 0.4f),
                1,
                editBox::getRectangle
        );
        this.keyboardBehaviour = createBehaviourWithLayout();
    }

    @Override
    public ComponentKeyboardBehaviour getKeyboardBehaviour(ScreenProcessor<?> screen, ControllerEntity controller) {
        return this.getKeyboardBehaviour();
    }

    public ComponentKeyboardBehaviour getKeyboardBehaviour() {
        return this.keyboardBehaviour;
    }

    public void setKeyboardBehaviour(ComponentKeyboardBehaviour keyboardBehaviour) {
        this.keyboardBehaviour = keyboardBehaviour;
    }

    public void setKeyboardLayout(KeyboardLayoutWithId layout) {
        this.keyboardLayout = layout;
        this.keyboardBehaviour = createBehaviourWithLayout();
    }

    public void setInputTarget(InputTarget inputTarget) {
        this.inputTarget = inputTarget;
        this.keyboardBehaviour = createBehaviourWithLayout();
    }

    public void setPositioner(KeyboardOverlayScreen.KeyboardPositioner positioner) {
        this.positioner = positioner;
        this.keyboardBehaviour = createBehaviourWithLayout();
    }

    @Override
    public boolean shouldKeepFocusOnKeyboardMode(ScreenProcessor<?> screen) {
        return true;
    }

    private ComponentKeyboardBehaviour createBehaviourWithLayout() {
        return new ComponentKeyboardBehaviour.Handled(
                this.keyboardLayout,
                this.inputTarget,
                this.positioner
        );
    }

    public record EditBoxInputTarget(EditBox editBox) implements InputTarget {

        @Override
        public boolean supportsCharInput() {
            return true;
        }

        @Override
        public boolean acceptChar(char ch, int modifiers) {
            this.editBox.charTyped(ch, modifiers);
            return true;
        }

        @Override
        public boolean supportsKeyCodeInput() {
            return true;
        }

        @Override
        public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
            this.editBox.keyPressed(keycode, scancode, modifiers);
            return true;
        }

        @Override
        public boolean supportsCursorMovement() {
            return true;
        }

        @Override
        public boolean moveCursor(int amount) {
            this.editBox.moveCursor(amount, false);
            return true;
        }

        @Override
        public boolean supportsCopying() {
            return true;
        }

        @Override
        public boolean copy() {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.editBox.getValue());
            return true;
        }
    }
}
