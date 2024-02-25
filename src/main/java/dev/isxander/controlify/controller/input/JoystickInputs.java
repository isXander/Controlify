package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.Controlify;
import net.minecraft.resources.ResourceLocation;

public final class JoystickInputs {
    private static final ResourceLocation[] BUTTONS = new ResourceLocation[256];
    private static final ResourceLocation[] AXES = new ResourceLocation[256];
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

    public static ResourceLocation axis(int index) {
        ResourceLocation cache = AXES[index];

        if (cache == null) {
            AXES[index] = cache = Controlify.id("axis/" + index);
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
