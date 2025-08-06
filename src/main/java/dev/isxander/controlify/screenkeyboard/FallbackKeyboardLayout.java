package dev.isxander.controlify.screenkeyboard;

import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static dev.isxander.controlify.screenkeyboard.KeyboardLayout.ShiftableKey;
import static dev.isxander.controlify.screenkeyboard.KeyboardLayout.Key.*;

public final class FallbackKeyboardLayout {
    public static final ResourceLocation ID = CUtil.rl("fallback");

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
                    k(SpecialKey.Action.BACKSPACE, 2.0f, ControlifyBindings.GUI_ABSTRACT_ACTION_1)
            ),
            List.of(
                    k(SpecialKey.Action.TAB, 1f, null),
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
                    k(SpecialKey.Action.ENTER, 2.0f, ControlifyBindings.GUI_ABSTRACT_ACTION_2)
            ),
            List.of(
                    k(SpecialKey.Action.SHIFT, 2f, ControlifyBindings.GUI_ABSTRACT_ACTION_3),
                    k("z"),
                    k("x"),
                    k("c"),
                    k("v"),
                    k("b"),
                    k("n"),
                    k("m"),
                    k(",", "."),
                    k("/", "\\"),
                    k(SpecialKey.Action.LEFT_ARROW, 1f, null),
                    k(SpecialKey.Action.RIGHT_ARROW, 1f, null)
            ),
            List.of(
                    k(SpecialKey.Action.UP_ARROW, 1f, null),
                    k(" ", 11f),
                    k(SpecialKey.Action.DOWN_ARROW, 1f, null)
            )
    );

    private static ShiftableKey k(SpecialKey.Action action, float width, @Nullable InputBindingSupplier shortcutBinding) {
        return new ShiftableKey(new SpecialKey(action), width, Optional.ofNullable(shortcutBinding));
    }
    private static ShiftableKey k(String string) {
        return new ShiftableKey(new StringKey(string));
    }
    private static ShiftableKey k(String regular, String shifted) {
        return new ShiftableKey(new StringKey(regular), new StringKey(shifted));
    }
    private static ShiftableKey k(String string, float width) {
        return new ShiftableKey(new StringKey(string), width);
    }
}
