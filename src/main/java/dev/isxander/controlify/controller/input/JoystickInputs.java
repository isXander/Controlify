package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public final class JoystickInputs {
    private static final Identifier[] BUTTONS = new Identifier[256];
    private static final Identifier[] AXES = new Identifier[512];
    private static final Identifier[] HATS = new Identifier[256];

    private JoystickInputs() {
    }

    public static Identifier button(int index) {
        Identifier cache = BUTTONS[index];

        if (cache == null) {
            BUTTONS[index] = cache = CUtil.rl("button/" + index);
        }

        return cache;
    }

    public static Identifier axis(int index, boolean positive) {
        int cacheIndex = index;
        if (!positive) {
            cacheIndex += 256;
        }

        Identifier cache = AXES[cacheIndex];

        if (cache == null) {
            AXES[cacheIndex] = cache = CUtil.rl("axis/" + index + "/" + (positive ? "positive" : "negative"));
        }

        return cache;
    }

    public static Identifier hat(int index) {
        Identifier cache = HATS[index];

        if (cache == null) {
            HATS[index] = cache = CUtil.rl("hat/" + index);
        }

        return cache;
    }
}
