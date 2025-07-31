package dev.isxander.controlify.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.mixins.feature.font.FontAccessor;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

//? if >=1.21.9 {
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.glyphs.BakeableGlyph;
//?}

public final class BindingFontHelper {
    public static final ResourceLocation WRAPPER_FONT = CUtil.rl("inputs");
    public static final String PLACEHOLDER_KEY = "controlify.placeholder";
    public static final String PLACEHOLDER_CONTROLLER_ACTIVE_KEY = "controlify.placeholder.controller_active";

    public static Component bindingWithFallback(ResourceLocation binding, Component fallback) {
        return Component.translatableWithFallback("controlify.placeholder", "%2$s", binding(binding), fallback);
    }

    public static Component bindingWithFallback(InputBinding binding, Component fallback) {
        return bindingWithFallback(binding.id(), fallback);
    }

    public static Component binding(ResourceLocation binding) {
        return Component.keybind(binding.toString()).withStyle(style -> style.withFont(WRAPPER_FONT));
    }

    public static Component binding(InputBinding binding) {
        return binding(binding.id());
    }

    public static int getComponentHeight(Font font, FormattedCharSequence text) {
        MutableInt mutableInt = new MutableInt();
        text.accept((index, style, codePoint) -> {
            mutableInt.setValue(Math.max(mutableInt.intValue(), getHeight(font, codePoint, style)));
            return true;
        });
        return mutableInt.intValue();
    }

    public static int getComponentHeight(Font font, FormattedText text) {
        MutableInt mutableInt = new MutableInt();
        StringDecomposer.iterateFormatted(text, Style.EMPTY, (index, style, codePoint) -> {
            mutableInt.setValue(Math.max(mutableInt.intValue(), getHeight(font, codePoint, style)));
            return true;
        });
        return mutableInt.intValue();
    }

    private static int getHeight(Font font, int codepoint, Style style) {
        //? if >=1.21.9 {
        BakeableGlyph glyph = ((FontAccessor) font).invokeGetGlyph(codepoint, style);

        var heightCapture = GlyphStitcherHeightCapture.INSTANCE;
        heightCapture.resetHeight(font.lineHeight + 5);
        glyph.info().bake(heightCapture);

        return heightCapture.height.get();
        //?} else {
        /*FontSet fontSet = ((FontAccessor) font).invokeGetFontSet(style.getFont());
        GlyphInfo glyphInfo = fontSet.getGlyphInfo(codepoint, false);
        // GlyphInfo does not expose height, hack a solution
        MutableInt f = new MutableInt(0);
        glyphInfo.bake(sheetGlyphInfo -> {
            f.setValue(sheetGlyphInfo.getPixelHeight());
            return null;
        });
        return f.intValue();
        *///?}
    }

    //? if >=1.21.9 {
    private static class GlyphStitcherHeightCapture extends GlyphStitcher {
        public static final GlyphStitcherHeightCapture INSTANCE = new GlyphStitcherHeightCapture();

        public final ThreadLocal<Integer> height;

        public GlyphStitcherHeightCapture() {
            super(null, null);
            this.height = new ThreadLocal<>();
            this.height.set(0);
        }

        public void resetHeight(int height) {
            this.height.set(height);
        }

        @Override
        public @Nullable BakedGlyph stitch(SheetGlyphInfo sheetGlyphInfo) {
            height.set(sheetGlyphInfo.getPixelHeight());
            return null;
        }

        @Override
        public void close() {}

        @Override
        public void reset() {}
    }
    //?}
}
