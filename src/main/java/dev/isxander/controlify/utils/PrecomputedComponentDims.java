package dev.isxander.controlify.utils;

import dev.isxander.controlify.font.BindingFontHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public record PrecomputedComponentDims<T>(T component, int width, int height) {
    public static PrecomputedComponentDims<Component> of(Component component, Font font) {
        int width = font.width(component);
        int height = BindingFontHelper.getComponentHeight(font, component);
        return new PrecomputedComponentDims<>(component, width, height);
    }

    public static PrecomputedComponentDims<FormattedCharSequence> of(FormattedCharSequence component, Font font) {
        int width = font.width(component);
        int height = BindingFontHelper.getComponentHeight(font, component);
        return new PrecomputedComponentDims<>(component, width, height);
    }
}
