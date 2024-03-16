package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.Controlify;
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
            BUTTONS[index] = cache = Controlify.id("button/" + index);
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
            AXES[cacheIndex] = cache = Controlify.id("axis/" + index + "/" + (positive ? "positive" : "negative"));
        }

        return cache;
    }

    public static ResourceLocation hat(int index) {
        ResourceLocation cache = HATS[index];

        if (cache == null) {
            HATS[index] = cache = Controlify.id("hat/" + index);
        }

        return cache;
    }
}
