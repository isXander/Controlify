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

    private Optional<Component> name = Optional.empty();

    public GuideActionRenderer(GuideAction<T> action, boolean rtl, boolean textContrast) {
        this.guideAction = action;
        this.rtl = rtl;
        this.textContrast = textContrast;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float deltaTime) {
        if (!isVisible())
            return;

        Font font = Minecraft.getInstance().font;
        int textWidth = name.map(font::width).orElse(0);
        int textHeight = name.map(c -> BindingFontHelper.getComponentHeight(font, c)).orElse(0);

        if (textContrast)
            graphics.fill(x - 1, y - 1, x + textWidth + 1, y + textHeight + 1, 0x80000000);
        graphics.drawString(font, name.get(), x, y + textHeight / 2 - font.lineHeight / 2, 0xFFFFFF, false);
    }

    @Override
    public Vector2ic size() {
        Font font = Minecraft.getInstance().font;
        return new Vector2i(name.map(font::width).orElse(0), Math.max(22, font.lineHeight));
    }

    @Override
    public boolean isVisible() {
        return name.isPresent() && !guideAction.binding().isUnbound();
    }

    public void updateName(T ctx) {
        name = guideAction.name().supply(ctx)
                .map(comp -> {
                    var component = Component.empty();
                    if (!rtl) component.append(BindingFontHelper.binding(guideAction.binding().id()));
                    component.append(comp);
                    if (rtl) component.append(BindingFontHelper.binding(guideAction.binding().id()));
                    return component;
                });
    }
}
