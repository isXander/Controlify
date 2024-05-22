package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

public final class JoystickInputs {
    private static final ResourceLocation[] BUTTONS = new ResourceLocation[256];
    private static final ResourceLocation[] AXES = new ResourceLocation[512];
    private static final ResourceLocation[] HATS = new ResourceLocation[256];

    private JoystickInputs() {
    }

    public static ResourceLocation button(int index) {
        ResourceLocation cache = BUTTONS[index];

        if (cache == null) {
            BUTTONS[index] = cache = CUtil.rl("button/" + index);
        }

        return cache;
    }

    public static ResourceLocation axis(int index, boolean positive) {
        int cacheIndex = index;
        if (!positive) {
            cacheIndex += 256;
        }

        ResourceLocation cache = AXES[cacheIndex];

        if (cache == null) {
            AXES[cacheIndex] = cache = CUtil.rl("axis/" + index + "/" + (positive ? "positive" : "negative"));
        }

        return cache;
    }

    public static ResourceLocation hat(int index) {
        ResourceLocation cache = HATS[index];

        if (cache == null) {
            HATS[index] = cache = CUtil.rl("hat/" + index);
        }

        return cache;
    }
}
