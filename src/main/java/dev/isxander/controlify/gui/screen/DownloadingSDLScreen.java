package dev.isxander.controlify.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.isxander.controlify.utils.ClientUtils;
import dev.isxander.controlify.utils.animation.api.EasingFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

import java.nio.file.Path;
import java.text.DecimalFormat;

public class DownloadingSDLScreen extends Screen implements DontInteruptScreen {
    private final Screen screenOnFinish;
    private final Path nativePath;

    private long receivedBytes;
    private long totalBytes;
    private final DecimalFormat format = new DecimalFormat("0.00 MB");

    public DownloadingSDLScreen(Screen screenOnFinish, long totalBytes, Path nativePath) {
        super(Component.translatable("controlify.downloading_sdl.title"));
        this.screenOnFinish = screenOnFinish;
        this.nativePath = nativePath;
        this.totalBytes = totalBytes;
    }

    @Override
    protected void init() {
        Component filePathText = Component.literal(nativePath.getFileName().toString())
                .withStyle(ChatFormatting.BLUE);
        addRenderableWidget(new PlainTextButton(
                width / 2 - font.width(filePathText) / 2,
                (int) (30 + 9 * 2.5f + 40 + 5 * 2f + 10),
                font.width(filePathText),
                font.lineHeight,
                filePathText,
                btn -> Util.getPlatform().openFile(nativePath.toFile()),
                font
        ));

        addRenderableWidget(new MultiLineTextWidget(
                width / 2 - (width - 50) / 2,
                (int) (30 + 9 * 2.5f + 40 + 5 * 2f + 10 + 9*3),
                Component.translatable("controlify.downloading_sdl.info"),
                font
        ).setMaxWidth(width - 20).setCentered(true));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        /*? if >1.20.4 {*/
        renderBackground(graphics, mouseX, mouseY, delta);
        /*? } else {*//*
        renderDirtBackground(graphics);
        *//*?}*/

        super.render(graphics, mouseX, mouseY, delta);

        graphics.pose().pushPose();
        graphics.pose().translate(width / 2f - font.width(this.getTitle()) / 2f * 2.5f, 30, 0);
        graphics.pose().scale(2.5f, 2.5f, 1f);

        graphics.drawString(font, this.getTitle(), 0, 0, -1);

        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().scale(2f, 2f, 1f);

        ClientUtils.drawBar(graphics, width / 2 / 2, (int) ((30 + 9 * 2.5f + 40) / 2), EasingFunction.EASE_OUT_EXPO.ease((float) ((double) receivedBytes / totalBytes)));

        graphics.pose().popPose();

        String totalString = format.format(totalBytes / 1024f / 1024f);
        graphics.drawString(
                font,
                totalString,
                (int) (width / 2f + 182 * 2f / 2 - font.width(totalString)),
                (int) (30 + 9 * 2f + 40 + 5 * 2f + 4),
                11184810 // light gray
        );

        String receivedString = format.format(receivedBytes / 1024f / 1024f);
        graphics.drawString(
                font,
                receivedString,
                (int) (width / 2f - 182 * 2f / 2),
                (int) (30 + 9 * 2f + 40 + 5 * 2f + 4),
                11184810 // light gray
        );


    }

    public void updateDownloadProgress(long receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    public void finishDownload() {
        minecraft.setScreen(screenOnFinish);
    }

    public void failDownload(Throwable th) {
        finishDownload();
    }

    public void increaseTotal(long increment) {
        this.totalBytes += increment;
    }
}
