package dev.isxander.controlify.screenkeyboard;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ChatKeyboardWidget extends KeyboardWidget<KeyboardWidget.Key> {

    public ChatKeyboardWidget(int x, int y, int width, int height, KeyPressConsumer keyPressConsumer) {
        super(x, y, width, height, keyPressConsumer);
    }

    @Override
    protected void arrangeKeys() {
        var builder = new KeyLayoutBuilder<>(14, 5, this);

        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_ESCAPE, "Esc")), 2f);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_1, '1')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_2, '2')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_3, '3')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_4, '4')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_5, '5')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_6, '6')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_7, '7')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_8, '8')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_9, '9')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_0, '0')), 1);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_BACKSPACE, "Backspace")), 2f);

        builder.nextRow();

        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_TAB, "Tab")), 2f);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_Q, 'q')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_W, 'w')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_E, 'e')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_R, 'r')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_T, 't')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_Y, 'y')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_U, 'u')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_I, 'i')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_O, 'o')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_P, 'p')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_BACKSLASH, '\\')), 2f);

        builder.nextRow();

        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_CAPSLOCK, "Caps")), 2f);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_A, 'a')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_S, 's')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_D, 'd')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_F, 'f')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_G, 'g')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_H, 'h')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_J, 'j')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_K, 'k')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_L, 'l')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_APOSTROPHE, '\'', 0, InputConstants.KEY_APOSTROPHE, '"', GLFW.GLFW_MOD_SHIFT)), 1);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_RETURN, "Enter")), 2f);

        builder.nextRow();

        builder.key(Key.builder(new KeyFunction((screen, key) -> {
            shiftMode = !shiftMode;
            key.setHighlighted(shiftMode);
        }, Key.ForegroundRenderer.text(Component.literal("Shift"))).copyShifted()), 2f);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_Z, 'z')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_X, 'x')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_C,'c')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_V, 'v')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_B, 'b')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_N, 'n')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_M, 'm')), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_COMMA, ',', 0, InputConstants.KEY_PERIOD, '.', 0)), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_1, '!', GLFW.GLFW_MOD_SHIFT, InputConstants.KEY_SLASH, '?', GLFW.GLFW_MOD_SHIFT)), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_SLASH, '/', 0, InputConstants.KEY_BACKSLASH, '\\', 0)), 1);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_UP, "↑")), 1f);

        builder.nextRow();

        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_LCONTROL, "Ctrl")), 2f);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_SPACE, ' ')), 9f);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_LEFT, "←")), 1f);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_DOWN, "↓")), 1f);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_RIGHT, "→")), 1f);

        builder.build(keys::add);
    }
}
