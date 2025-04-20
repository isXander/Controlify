package dev.isxander.controlify.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.isxander.controlify.utils.render.Blit;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class ClientUtils {
    private static final ResourceLocation GREEN_BACK_BAR = ResourceLocation.withDefaultNamespace("boss_bar/green_background");
    private static final ResourceLocation GREEN_FRONT_BAR = ResourceLocation.withDefaultNamespace("boss_bar/green_progress");

    private ClientUtils() {
    }

    public static StringWidget createStringWidget(Component text, Font font, int x, int y) {
        return new StringWidget(x, y, font.width(text.getVisualOrderText()), font.lineHeight, text, font);
    }

    public static PlainTextButton createPlainTextButton(Component text, Font font, int x, int y, Button.OnPress onPress) {
        return new PlainTextButton(x, y, font.width(text.getVisualOrderText()), font.lineHeight, text, onPress, font);
    }

    public static void drawSprite(GuiGraphics graphics, ResourceLocation location, int x, int y, int width, int height) {
        Blit.blitSprite(graphics, location, x, y, width, height);
    }

    public static void drawBar(GuiGraphics graphics, int centerX, int y, float progress) {
        int width = (int) Mth.clampedLerp(0, 182, progress);

        int x = centerX - 182 / 2;

        Blit.blitSprite(graphics, GREEN_BACK_BAR, 182, 5, 0, 0, x, y, 182, 5);
        if (width > 0) {
            Blit.blitSprite(graphics, GREEN_FRONT_BAR, 182, 5, 0, 0, x, y, width, 5);
        }
    }
}
