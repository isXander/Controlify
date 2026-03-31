package dev.isxander.controlify.font;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.mutable.MutableInt;

public final class BindingFontHelper {
    public static final Identifier WRAPPER_FONT = CUtil.rl("inputs");
    public static final String PLACEHOLDER_KEY = "controlify.placeholder";
    public static final String PLACEHOLDER_CONTROLLER_ACTIVE_KEY = "controlify.placeholder.controller_active";

    public static Component bindingWithActiveFallback(Identifier binding, Component fallback) {
        return Component.translatableWithFallback(PLACEHOLDER_CONTROLLER_ACTIVE_KEY, "%2$s", binding(binding), fallback);
    }

    public static Component bindingWithActiveFallback(InputBinding binding, Component fallback) {
        return bindingWithActiveFallback(binding.id(), fallback);
    }

    public static Component bindingWithFallback(Identifier binding, Component fallback) {
        return Component.translatableWithFallback("controlify.placeholder", "%2$s", binding(binding), fallback);
    }

    public static Component bindingWithFallback(InputBinding binding, Component fallback) {
        return bindingWithFallback(binding.id(), fallback);
    }

    public static Component binding(Identifier binding) {
        return Component.keybind(binding.toString()).withStyle(style -> style.withFont(new FontDescription.Resource(WRAPPER_FONT)));
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
        // i can't figure out how to get the height here
        return 15;
    }
}
