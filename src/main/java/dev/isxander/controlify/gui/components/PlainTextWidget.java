package dev.isxander.controlify.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PlainTextWidget extends AbstractWidget {

    public PlainTextWidget(Component text) {
        super(0, 0, 0, 0, text);
        this.setWidth(Minecraft.getInstance().font.width(text));
        this.setHeight(Minecraft.getInstance().font.lineHeight + 2);
    }

    @Override
    public void setMessage(@NonNull Component message) {
        super.setMessage(message);
        this.setWidth(Minecraft.getInstance().font.width(message));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR)
                .accept(getX(), getY() + 1, getMessage());
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(@NonNull FocusNavigationEvent event) {
        return null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }
}
