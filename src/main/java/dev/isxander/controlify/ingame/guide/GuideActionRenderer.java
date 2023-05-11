package dev.isxander.controlify.ingame.guide;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.isxander.controlify.api.bind.BindRenderer;
import dev.isxander.controlify.api.ingameguide.IngameGuideContext;
import dev.isxander.controlify.gui.DrawSize;
import dev.isxander.controlify.gui.layout.RenderComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Optional;

public class GuideActionRenderer implements RenderComponent {
    private final GuideAction guideAction;
    private final boolean rtl;

    private Optional<Component> name = Optional.empty();

    public GuideActionRenderer(GuideAction action, boolean rtl) {
        this.guideAction = action;
        this.rtl = rtl;
    }

    @Override
    public void render(PoseStack stack, int x, int y, float deltaTime) {
        if (name.isEmpty())
            return;

        Font font = Minecraft.getInstance().font;

        BindRenderer renderer = guideAction.binding().renderer();
        DrawSize drawSize = renderer.size();
        int textWidth = font.width(name.get());

        renderer.render(stack, x + (!rtl ? 0 : textWidth + 2), y + drawSize.height() / 2);

        int textX = x + (rtl ? 0 : drawSize.width() + 2);
        int textY = y + drawSize.height() / 2 - font.lineHeight / 2;

        GuiComponent.fill(stack, textX - 1, textY - 1, textX + textWidth + 1, textY + font.lineHeight + 1, 0x80000000);
        font.draw(stack, name.get(), textX, textY, 0xFFFFFF);
    }

    @Override
    public Vector2ic size() {
        DrawSize bindSize = guideAction.binding().renderer().size();
        Font font = Minecraft.getInstance().font;

        return new Vector2i(bindSize.width() + 2 + name.map(font::width).orElse(-2), Math.max(bindSize.height(), font.lineHeight));
    }

    @Override
    public boolean isVisible() {
        return name.isPresent();
    }

    public void updateName(IngameGuideContext ctx) {
        name = guideAction.name().supply(ctx);
    }
}
