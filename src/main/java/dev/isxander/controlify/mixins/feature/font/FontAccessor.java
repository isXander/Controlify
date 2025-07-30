package dev.isxander.controlify.mixins.feature.font;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Font.class)
public interface FontAccessor {
    //? if <1.21.9 {
    /*// TODO: figure out how to make this work with 1.21.9+
    @Invoker
    FontSet invokeGetFontSet(ResourceLocation id);
    *///?}
}
