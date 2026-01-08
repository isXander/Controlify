package dev.isxander.controlify.screenop.keyboard;

import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static dev.isxander.controlify.screenop.keyboard.KeyboardLayout.Key;
import static dev.isxander.controlify.screenop.keyboard.KeyboardLayout.KeyFunction.*;

/**
 * A code-defined fallback keyboard layout if the resource-pack driven ones
 * are not available or the layout required is not defined.
 */
public final class FallbackKeyboardLayout {
    public static final Identifier ID = CUtil.rl("fallback");

    public static final KeyboardLayout QWERTY = KeyboardLayout.of(13.0f,
            List.of(
                    k("§", "±"),
                    k("q"),
                    k("w"),
                    k("e"),
                    k("r"),
                    k("t"),
                    k("y"),
                    k("u"),
                    k("i"),
                    k("o"),
                    k("p"),
                    k(SpecialFunc.Action.BACKSPACE, 2.0f, ControlifyBindings.GUI_ABSTRACT_ACTION_1)
            ),
            List.of(
                    k(SpecialFunc.Action.TAB, 1f, null),
                    k("a"),
                    k("s"),
                    k("d"),
                    k("f"),
                    k("g"),
                    k("h"),
                    k("j"),
                    k("k"),
                    k("l"),
                    k("'", "\""),
                    k(SpecialFunc.Action.ENTER, 2.0f, ControlifyBindings.GUI_ABSTRACT_ACTION_2)
            ),
            List.of(
                    k(SpecialFunc.Action.SHIFT, 2f, ControlifyBindings.GUI_ABSTRACT_ACTION_3),
                    k("z"),
                    k("x"),
                    k("c"),
                    k("v"),
                    k("b"),
                    k("n"),
                    k("m"),
                    k(",", "."),
                    k("/", "\\"),
                    k(SpecialFunc.Action.LEFT_ARROW, 1f, null),
                    k(SpecialFunc.Action.RIGHT_ARROW, 1f, null)
            ),
            List.of(
                    k(SpecialFunc.Action.UP_ARROW, 1f, null),
                    k(" ", 11f),
                    k(SpecialFunc.Action.DOWN_ARROW, 1f, null)
            )
    );

    private static Key k(SpecialFunc.Action action, float width, @Nullable InputBindingSupplier shortcutBinding) {
        return new Key(new SpecialFunc(action), width, Optional.ofNullable(shortcutBinding));
    }
    private static Key k(String string) {
        return new Key(new StringFunc(string));
    }
    private static Key k(String regular, String shifted) {
        return new Key(new StringFunc(regular), new StringFunc(shifted));
    }
    private static Key k(String string, float width) {
        return new Key(new StringFunc(string), width);
    }
}
