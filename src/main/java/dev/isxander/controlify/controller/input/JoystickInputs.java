package dev.isxander.controlify.controller.input;

import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.stream.IntStream;

public final class JoystickInputs {
    private static final ResourceLocation[] BUTTONS = IntStream.range(0, 64)
            .mapToObj(i -> CUtil.rl("button/" + i))
            .toArray(ResourceLocation[]::new);
    private static final ResourceLocation[] AXES = IntStream.range(0, 128)
            .mapToObj(i -> {
                // least significant bit is sign bit
                boolean isPositive = (i & 1) == 0;
                int axisIndex = i >> 1;
                return CUtil.rl("axis/" + axisIndex + "/" + (isPositive ? "positive" : "negative"));
            })
            .toArray(ResourceLocation[]::new);
    private static final ResourceLocation[] HATS = IntStream.range(0, 64)
            .mapToObj(i -> CUtil.rl("hat/" + i))
            .toArray(ResourceLocation[]::new);

    private JoystickInputs() {
    }

    public static ResourceLocation button(int index) {
        return BUTTONS[index];
    }

    public static ResourceLocation axis(int index, boolean positive) {
        return AXES[(index << 1) | (positive ? 0 : 1)];
    }

    public static ResourceLocation hat(int index) {
        return HATS[index];
    }
}
