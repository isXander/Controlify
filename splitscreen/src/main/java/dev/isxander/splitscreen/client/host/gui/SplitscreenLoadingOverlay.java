package dev.isxander.splitscreen.client.host.gui;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.Optional;
import java.util.function.Consumer;

public class SplitscreenLoadingOverlay extends Overlay {

    private static final int BACKGROUND_COLOUR = ARGB.color(250, 254, 140);
    private static final int FOREGROUND_COLOUR = ARGB.color(35, 35, 35);
    private static final long FADE_OUT_TIME = 1000L;
    private static final long FADE_IN_TIME = 500L;

    private final Minecraft minecraft;
    private final SplitscreenFakeReloadInstance status;

    private float progress;

    private final boolean fadeIn;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;

    private final Consumer<Optional<Throwable>> onFinish;

    public SplitscreenLoadingOverlay(Minecraft minecraft, SplitscreenFakeReloadInstance status, Consumer<Optional<Throwable>> onFinish, boolean fadeIn) {
        this.minecraft = minecraft;
        this.status = status;
        this.onFinish = onFinish;
        this.fadeIn = fadeIn;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 1000);

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        long time = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = time;
        }

        float fadeOutProgress = this.fadeOutStart > 1L ? (float) (time - this.fadeOutStart) / FADE_OUT_TIME : -1.0F;
        float fadeInProgress = this.fadeInStart > 1L ? (float) (time - this.fadeInStart) / FADE_IN_TIME : -1.0F;

        float logoFade;
        if (fadeOutProgress >= 1.0F) {
            this.renderScreen(guiGraphics, partialTick);

            logoFade = 1 - Mth.clamp(fadeOutProgress - 1, 0, 1);
            int opacity = Mth.ceil(logoFade * 255);
            guiGraphics.fill(RenderType.guiOverlay(), 0, 0, width, height, ARGB.color(opacity, BACKGROUND_COLOUR));
        } else if (this.fadeIn) {
            this.renderScreen(guiGraphics, partialTick);

            int alpha = Mth.ceil(Mth.clamp(fadeInProgress, 0.15, 1.0) * 255);
            guiGraphics.fill(RenderType.guiOverlay(), 0, 0, width, height, ARGB.color(alpha, BACKGROUND_COLOUR));
            logoFade = Mth.clamp(fadeInProgress, 0, 1);
        } else {
            int backgroundColor = BACKGROUND_COLOUR;
            guiGraphics.fill(RenderType.guiOverlay(), 0, 0, width, height, backgroundColor);
            logoFade = 1;
        }

        int centerX = width / 2;
        int centerY = height / 2;
        double d = Math.min(width * 0.75, height) * 0.25;
        double barWidth = d * 4;
        int halfBarWidth = (int) (barWidth / 2);
        int barY = (int) (height * 0.8325f);

        guiGraphics.drawString(this.minecraft.font, "Loading Splitscreen", centerX, centerY - 10, FOREGROUND_COLOUR, false);

        float progress = this.status.getActualProgress();
        this.progress = Mth.clamp(this.progress * 0.95f + progress * 0.05f, 0, 1);
        if (fadeOutProgress < 1f) {
            this.drawProgressBar(guiGraphics, centerX - halfBarWidth, barY - 5, centerX + halfBarWidth, barY + 5, 1 - Mth.clamp(fadeOutProgress, 0, 1));
        }
        if (fadeOutProgress >= 2f) {
            this.minecraft.setOverlay(null);
        }

        if (this.fadeOutStart == -1L && this.status.isDone() && (!this.fadeIn || fadeInProgress >= 2.0F)) {
            try {
                this.status.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable var24) {
                this.onFinish.accept(Optional.of(var24));
            }

            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, guiGraphics.guiWidth(), guiGraphics.guiHeight());
            }
        }

        guiGraphics.pose().popPose();
    }

    private void renderScreen(GuiGraphics guiGraphics, float partialTick) {
        if (this.minecraft.screen != null) {
            this.minecraft.screen.render(guiGraphics, 0, 0, partialTick);
        }
    }

    private void drawProgressBar(GuiGraphics guiGraphics, int minX, int minY, int maxX, int maxY, float partialTick) {
        int i = Mth.ceil((maxX - minX - 2) * this.progress);
        int j = Math.round(partialTick * 255.0F);
        int k = ARGB.color(j, FOREGROUND_COLOUR);
        guiGraphics.fill(minX + 2, minY + 2, minX + i, maxY - 2, k);
        guiGraphics.fill(minX + 1, minY, maxX - 1, minY + 1, k);
        guiGraphics.fill(minX + 1, maxY, maxX - 1, maxY - 1, k);
        guiGraphics.fill(minX, minY, minX + 1, maxY, k);
        guiGraphics.fill(maxX, minY, maxX - 1, maxY, k);
    }

}
