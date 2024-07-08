package dev.isxander.controlify.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class ClientUtils {
    /*? if >=1.20.3 {*/
    private static final ResourceLocation GREEN_BACK_BAR = CUtil.mcRl("boss_bar/green_background");
    private static final ResourceLocation GREEN_FRONT_BAR = CUtil.mcRl("boss_bar/green_progress");
    /*?} else {*/
    /*private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");
    *//*?}*/

    private ClientUtils() {
    }

    public static StringWidget createStringWidget(Component text, Font font, int x, int y) {
        return new StringWidget(x, y, font.width(text.getVisualOrderText()), font.lineHeight, text, font);
    }

    public static PlainTextButton createPlainTextButton(Component text, Font font, int x, int y, Button.OnPress onPress) {
        return new PlainTextButton(x, y, font.width(text.getVisualOrderText()), font.lineHeight, text, onPress, font);
    }

    public static void drawSprite(GuiGraphics graphics, ResourceLocation location, int x, int y, int width, int height) {
        /*? if >=1.20.3 {*/
        graphics.blitSprite(location, x, y, width, height);
        /*?} else {*/
        /*graphics.blit(location, x, y, 0, 0, width, height, width, height);
        *//*?}*/
    }

    public static void drawBar(GuiGraphics graphics, int centerX, int y, float progress) {
        int width = (int) Mth.clampedLerp(0, 182, progress);

        int x = centerX - 182 / 2;

        /*? if >=1.20.3 {*/
        graphics.blitSprite(GREEN_BACK_BAR, 182, 5, 0, 0, x, y, 182, 5);
        if (width > 0) {
            graphics.blitSprite(GREEN_FRONT_BAR, 182, 5, 0, 0, x, y, width, 5);
        }
        /*?} else {*/
        /*graphics.blit(GUI_BARS_LOCATION, x, y, 0, 30, 182, 5);
        if (width > 0) {
            RenderSystem.enableBlend();
            graphics.blit(GUI_BARS_LOCATION, x, y, 0, 35, width, 5);
            RenderSystem.disableBlend();
        }
        *//*?}*/
    }
}
