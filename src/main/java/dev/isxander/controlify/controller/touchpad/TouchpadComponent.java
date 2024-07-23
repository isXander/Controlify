package dev.isxander.controlify.controller.touchpad;

import dev.isxander.controlify.controller.ECSComponent;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TouchpadComponent implements ECSComponent {
    public static final ResourceLocation ID = CUtil.rl("touchpad");

    private final Touchpads touchpads;

    public TouchpadComponent(Touchpads touchpads) {
        this.touchpads = touchpads;
    }

    public Touchpads.Touchpad[] touchpads() {
        return touchpads.touchpads();
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
