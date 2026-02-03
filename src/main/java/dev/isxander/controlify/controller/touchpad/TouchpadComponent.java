package dev.isxander.controlify.controller.touchpad;

import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class TouchpadComponent extends ECSComponentImpl {
    public static final Identifier ID = CUtil.rl("touchpad");

    private final Touchpads touchpads;

    public TouchpadComponent(Touchpads touchpads) {
        this.touchpads = touchpads;
    }

    public Touchpads.Touchpad[] touchpads() {
        return touchpads.touchpads();
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
