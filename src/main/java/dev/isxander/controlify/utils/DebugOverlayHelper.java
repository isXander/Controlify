package dev.isxander.controlify.utils;

import dev.isxander.controlify.mixins.feature.input.DebugScreenOverlayAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class DebugOverlayHelper {
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isOverlayEnabled() {
        //? if >=1.20.3 {
        return ((DebugScreenOverlayAccessor) mc.getDebugOverlay()).isRenderDebug();
        //?} else {
        /*return mc.options.renderDebug;
        *///?}
    }

    public static void toggleOverlay() {
        //? if >=1.20.3 {
        mc.getDebugOverlay().toggleOverlay();
        //?} else {
        /*mc.options.renderDebug = !mc.options.renderDebug;
        *///?}
    }

    public static void toggleFpsOverlay() {
        //? if >=1.20.3 {
        mc.getDebugOverlay().toggleFpsCharts();
        //?} else {
        /*mc.options.renderDebug = mc.options.renderFpsChart = !mc.options.renderDebug;
        *///?}
    }

    //? if >=1.20.3 {
    public static void toggleNetworkOverlay() {
        mc.getDebugOverlay().toggleNetworkCharts();
    }

    public static void toggleProfilerOverlay() {
        mc.getDebugOverlay().toggleProfilerChart();
    }
    //?} else {
    /*public static void toggleChartsOverlay() {
        mc.options.renderDebug = mc.options.renderCharts = !mc.options.renderDebug;
    }
    *///?}

    public static void reloadChunks() {
        mc.levelRenderer.allChanged();
        debugFeedbackTranslated("debug.reload_chunks.message");
    }

    public static void toggleChunkBorders() {
        boolean flag = mc.debugRenderer.switchRenderChunkborder();
        debugFeedbackTranslated(flag ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
    }

    public static void toggleAdvancedTooltips() {
        boolean enabled = mc.options.advancedItemTooltips = !mc.options.advancedItemTooltips;
        mc.options.save();
        debugFeedbackTranslated(enabled ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
    }

    public static void toggleEntityHitboxes() {
        boolean flag = !mc.getEntityRenderDispatcher().shouldRenderHitBoxes();
        mc.getEntityRenderDispatcher().setRenderHitBoxes(flag);
        debugFeedbackTranslated(flag ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
    }

    public static void reloadResourcePacks() {
        debugFeedbackTranslated("debug.reload_resourcepacks.message");
        mc.reloadResourcePacks();
    }

    public static void startStopProfiling() {
        if (mc.debugClientMetricsStart(DebugOverlayHelper::debugFeedbackComponent)) {
            debugFeedbackTranslated("debug.profiling.start", 10);
        }
    }

    public static void clearChat() {
        mc.gui.getChat().clearMessages(false);
    }

    private static void debugComponent(ChatFormatting formatting, Component message) {
        mc
                .gui
                .getChat()
                .addMessage(
                        Component.empty().append(Component.translatable("debug.prefix").withStyle(formatting, ChatFormatting.BOLD)).append(CommonComponents.SPACE).append(message)
                );
    }

    private static void debugFeedbackComponent(Component message) {
        debugComponent(ChatFormatting.YELLOW, message);
    }

    private static void debugFeedbackTranslated(String message, Object... args) {
        debugFeedbackComponent(Component.translatableEscape(message, args));
    }
}
