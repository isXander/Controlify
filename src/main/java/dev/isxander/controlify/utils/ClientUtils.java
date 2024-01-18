package dev.isxander.controlify.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class ClientUtils {
    private static final ResourceLocation GREEN_BACK_BAR = new ResourceLocation("boss_bar/green_background");
    private static final ResourceLocation GREEN_FRONT_BAR = new ResourceLocation("boss_bar/green_progress");

    private ClientUtils() {
    }

    public static StringWidget createStringWidget(Component text, Font font, int x, int y) {
        return new StringWidget(x, y, font.width(text.getVisualOrderText()), font.lineHeight, text, font);
    }

    public static PlainTextButton createPlainTextButton(Component text, Font font, int x, int y, Button.OnPress onPress) {
        return new PlainTextButton(x, y, font.width(text.getVisualOrderText()), font.lineHeight, text, onPress, font);
    }

    public static void drawBar(GuiGraphics graphics, int centerX, int y, float progress) {
        int width = Mth.lerpDiscrete(progress, 0, 182);

        int x = centerX - 182 / 2;
        graphics.blitSprite(GREEN_BACK_BAR, 182, 5, 0, 0, x, y, 182, 5);
        if (width > 0) {
            graphics.blitSprite(GREEN_FRONT_BAR, 182, 5, 0, 0, x, y, width, 5);
        }
    }
}
