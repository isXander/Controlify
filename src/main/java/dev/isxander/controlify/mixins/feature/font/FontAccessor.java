package dev.isxander.controlify.mixins.feature.font;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

@Mixin(Font.class)
public interface FontAccessor {
    @Invoker("getGlyph")
    BakedGlyph controlfy$invokeGetGlyph(int i, Style style);
}
