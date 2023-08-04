package dev.isxander.controlify.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public final class ClientUtils {
    private ClientUtils() {
    }

    public static StringWidget createStringWidget(Component text, Font font, int x, int y) {
        return new StringWidget(x, y, font.width(text.getVisualOrderText()), font.lineHeight, text, font);
    }

    public static PlainTextButton createPlainTextButton(Component text, Font font, int x, int y, Button.OnPress onPress) {
        return new PlainTextButton(x, y, font.width(text.getVisualOrderText()), font.lineHeight, text, onPress, font);
    }
}
