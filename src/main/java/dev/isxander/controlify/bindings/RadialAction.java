package dev.isxander.controlify.bindings;

import dev.isxander.controlify.gui.screen.RadialMenuScreen;
import net.minecraft.resources.ResourceLocation;

public record RadialAction(ResourceLocation binding, ResourceLocation icon) {
    public static final RadialAction EMPTY = new RadialAction(
            RadialMenuScreen.EMPTY,
            RadialIcons.EMPTY
    );
}
