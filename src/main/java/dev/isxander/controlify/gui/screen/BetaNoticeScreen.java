package dev.isxander.controlify.gui.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AccessibilityOnboardingTextWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BetaNoticeScreen extends Screen {

    public BetaNoticeScreen() {
        super(Component.translatable("controlify.beta.title"));
    }

    @Override
    protected void init() {
        MultiLineTextWidget textWidget = new AccessibilityOnboardingTextWidget(
                font,
                Component.translatable("controlify.beta.message",
                        Component.translatable("controlify.beta.message.link")
                                .withStyle(ChatFormatting.AQUA)
                ),
                this.width - 10
        );
        textWidget.setX(this.width / 2 - textWidget.getWidth() / 2);
        textWidget.setY(30);
        addRenderableWidget(textWidget);

        addRenderableWidget(
                Button.builder(
                        Component.translatable("controlify.beta.button"),
                        btn -> Util.getPlatform().openUri("https://github.com/isXander/controlify/issues")
                )
                        .pos(this.width / 2 - 75, this.height - 8 - 20 - 20 - 4)
                        .width(150)
                        .build()
        );
        addRenderableWidget(
                Button.builder(
                        CommonComponents.GUI_CONTINUE,
                        btn -> minecraft.setScreen(new TitleScreen())
                )
                        .pos(this.width / 2 - 75, this.height - 8 - 20)
                        .width(150)
                        .build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
    }
}
