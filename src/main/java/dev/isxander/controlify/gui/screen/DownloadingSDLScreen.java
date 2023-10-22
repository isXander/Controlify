package dev.isxander.controlify.gui.screen;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.driver.SDL2NativesManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;

public class DownloadingSDLScreen extends Screen implements DontInteruptScreen {
    private static final ResourceLocation GREEN_BACK_BAR = new ResourceLocation("boss_bar/green_background");
    private static final ResourceLocation GREEN_FRONT_BAR = new ResourceLocation("boss_bar/green_progress");

    private final Screen screenOnFinish;
    private final Path nativePath;

    private long receivedBytes;
    private final long totalBytes;
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
        renderDirtBackground(graphics);

        super.render(graphics, mouseX, mouseY, delta);

        graphics.pose().pushPose();
        graphics.pose().translate(width / 2f - font.width(this.getTitle()) / 2f * 2.5f, 30, 0);
        graphics.pose().scale(2.5f, 2.5f, 1f);

        graphics.drawString(font, this.getTitle(), 0, 0, -1);

        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().scale(2f, 2f, 1f);

        drawBar(graphics, width / 2 / 2, (int) ((30 + 9 * 2.5f + 40) / 2), (float) ((double) receivedBytes / totalBytes));

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

    @Override
    public void added() {
        CompletableFuture<Boolean> askNativesFuture = Controlify.instance().askNatives();
        if (askNativesFuture.isDone()) {
            minecraft.setScreen(screenOnFinish);
        }
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

    private void drawBar(GuiGraphics graphics, int centerX, int y, float progress) {
        int width = Mth.lerpDiscrete(1 - (float)Math.pow(1 - progress, 3), 0, 182);

        int x = centerX - 182 / 2;
        graphics.blitSprite(GREEN_BACK_BAR, 182, 5, 0, 0, x, y, 182, 5);
        if (width > 0) {
            graphics.blitSprite(GREEN_FRONT_BAR, 182, 5, 0, 0, x, y, width, 5);
        }
    }
}
