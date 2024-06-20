package dev.isxander.controlify.screenkeyboard;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.bindings.ControlifyBindings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ChatKeyboardWidget extends KeyboardWidget<KeyboardWidget.Key> {

    public ChatKeyboardWidget(Screen screen, int x, int y, int width, int height, KeyPressConsumer keyPressConsumer) {
        super(screen, x, y, width, height, keyPressConsumer);
    }

    @Override
    protected void arrangeKeys() {
        var builder = new KeyLayoutBuilder<>(14, 5, this);

        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_ESCAPE, "Esc"), null), 1f);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_1, '1'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_2, '2'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_3, '3'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_4, '4'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_5, '5'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_6, '6'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_7, '7'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_8, '8'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_9, '9'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_0, '0'), null), 1);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_BACKSPACE, "Backspace"), ControlifyBindings.GUI_ABSTRACT_ACTION_1), 3f);

        builder.nextRow();

        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_TAB, "Tab"), null), 2f);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_Q, 'q'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_W, 'w'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_E, 'e'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_R, 'r'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_T, 't'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_Y, 'y'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_U, 'u'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_I, 'i'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_O, 'o'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_P, 'p'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_BACKSLASH, '\\'), null), 2f);

        builder.nextRow();

        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_CAPSLOCK, "Caps"), null), 2f);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_A, 'a'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_S, 's'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_D, 'd'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_F, 'f'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_G, 'g'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_H, 'h'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_J, 'j'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_K, 'k'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_L, 'l'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_APOSTROPHE, '\'', 0, InputConstants.KEY_APOSTROPHE, '"', GLFW.GLFW_MOD_SHIFT), null), 1);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_RETURN, "Enter"), ControlifyBindings.GUI_ABSTRACT_ACTION_2), 2f);

        builder.nextRow();

        builder.key(Key.builder(new KeyFunction((screen, key) -> {
            shiftMode = !shiftMode;
            key.setHighlighted(shiftMode);
        }, Key.ForegroundRenderer.text(Component.literal("Shift"))).copyShifted(), ControlifyBindings.GUI_ABSTRACT_ACTION_3), 2f);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_Z, 'z'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_X, 'x'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_C,'c'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_V, 'v'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_B, 'b'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_N, 'n'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_M, 'm'), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_COMMA, ',', 0, InputConstants.KEY_PERIOD, '.', 0), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_1, '!', GLFW.GLFW_MOD_SHIFT, InputConstants.KEY_SLASH, '?', GLFW.GLFW_MOD_SHIFT), null), 1);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_SLASH, '/', 0, InputConstants.KEY_BACKSLASH, '\\', 0), null), 1);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_UP, "\u2191"), null), 1f);

        builder.nextRow();

        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_LCONTROL, "Ctrl"), null), 2f);
        builder.key(Key.builder(KeyFunction.ofChar(InputConstants.KEY_SPACE, ' '), null), 9f);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_LEFT, "\u2190"), null), 1f);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_DOWN, "\u2193"), null), 1f);
        builder.key(Key.builder(KeyFunction.ofRegularKey(InputConstants.KEY_RIGHT, "\u2192"), null), 1f);

        builder.build(keys::add);
    }
}
