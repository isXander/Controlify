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

    public EditBoxComponentProcessor(EditBox editBox, int screenWidth, int screenHeight) {
        this.editBox = editBox;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.keyboardBehaviour = createBehaviourWithLayout(editBox, KeyboardLayouts.simple(), screenWidth, screenHeight);
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
        this.setKeyboardBehaviour(createBehaviourWithLayout(this.editBox, layout, this.screenWidth, this.screenHeight));
    }

    @Override
    public boolean shouldKeepFocusOnKeyboardMode(ScreenProcessor<?> screen) {
        return true;
    }

    private static ComponentKeyboardBehaviour createBehaviourWithLayout(EditBox editBox, KeyboardLayoutWithId layout, int screenWidth, int screenHeight) {
        return new ComponentKeyboardBehaviour.Handled(
                layout,
                new EditBoxInputTarget(editBox),
                KeyboardOverlayScreen.aboveOrBelowWidgetPositioner(
                        (int) (screenWidth * 0.8f), (int) (screenHeight * 0.4f),
                        1,
                        editBox::getRectangle
                )
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
