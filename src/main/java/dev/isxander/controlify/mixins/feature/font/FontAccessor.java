package dev.isxander.controlify.mixins.feature.font;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//? if >=1.21.9
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

@Mixin(Font.class)
public interface FontAccessor {
    //? if >=1.21.9 {
    @Invoker
    BakedGlyph invokeGetGlyph(int i, Style style);
    //?} else {
    /*@Invoker
    FontSet invokeGetFontSet(Identifier id);
    *///?}
}
