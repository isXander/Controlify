package dev.isxander.controlify.gui.guide;

import dev.isxander.controlify.font.BindingFontHelper;
import dev.isxander.controlify.gui.layout.RenderComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Optional;

public class GuideActionRenderer<T> implements RenderComponent {
    private final GuideAction<T> guideAction;
    private final boolean rtl;
    private final boolean textContrast;

    private Component bindingText;
    private int bindingTextWidth;
    private Component name = null;

    public GuideActionRenderer(GuideAction<T> action, boolean rtl, boolean textContrast) {
        this.guideAction = action;
        this.rtl = rtl;
        this.textContrast = textContrast;
        this.bindingText = action.binding().inputIcon();
        this.bindingTextWidth = Minecraft.getInstance().font.width(bindingText);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float deltaTime) {
        if (!isVisible())
            return;

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(name);
        int bindingHeight = BindingFontHelper.getComponentHeight(font, bindingText);
        int centeredTextY = y + bindingHeight / 2 - font.lineHeight / 2;

        if (!rtl) {
            graphics.drawString(font, bindingText, x, centeredTextY, -1, false);
            x += bindingTextWidth + 4;
        }

        if (textContrast) {
            graphics.fill(x - 1, centeredTextY - 1, x + textWidth + 1, centeredTextY + font.lineHeight + 1, 0x80000000);
        }
        graphics.drawString(font, name, x, centeredTextY, -1, false);
        x += textWidth + 4;
        if (rtl) {
            graphics.drawString(font, bindingText, x, centeredTextY, -1, false);
        }
    }

    @Override
    public Vector2ic size() {
        if (!isVisible()) return new Vector2i();

        Font font = Minecraft.getInstance().font;
        return new Vector2i(
                font.width(name) + 4 + bindingTextWidth,
                Math.max(BindingFontHelper.getComponentHeight(font, bindingText), font.lineHeight) + 2
        );
    }

    @Override
    public boolean isVisible() {
        return name != null && !guideAction.binding().isUnbound() && bindingText != null;
    }

    public void updateName(T ctx) {
        this.bindingText = guideAction.binding().inputIcon();
        this.bindingTextWidth = Minecraft.getInstance().font.width(bindingText);
        name = guideAction.name().supply(ctx).orElse(null);
    }
}
