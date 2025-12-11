package dev.isxander.controlify.utils;

import dev.isxander.controlify.mixins.feature.input.DebugScreenOverlayAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

//? if >=1.21.9
import net.minecraft.client.gui.components.debug.DebugScreenEntries;

public final class DebugOverlayHelper {
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isOverlayEnabled() {
        //? if >=1.21.11 {
        return mc.debugEntries.isOverlayVisible();
        //?} elif >=1.21.9 {
        /*return mc.debugEntries.isF3Visible();
        *///?} else {
        /*return ((DebugScreenOverlayAccessor) mc.getDebugOverlay()).isRenderDebug();
        *///?}
    }

    public static void toggleOverlay() {
        //? if >=1.21.11 {
        mc.debugEntries.toggleDebugOverlay();
        //?} elif >=1.21.9 {
        /*mc.debugEntries.toggleF3Visible();
        *///?} else {
        /*mc.getDebugOverlay().toggleOverlay();
        *///?}
    }

    public static void toggleFpsOverlay() {
        mc.getDebugOverlay().toggleFpsCharts();
    }

    public static void toggleNetworkOverlay() {
        mc.getDebugOverlay().toggleNetworkCharts();
    }

    public static void toggleProfilerOverlay() {
        mc.getDebugOverlay().toggleProfilerChart();
    }

    public static void reloadChunks() {
        mc.levelRenderer.allChanged();
        debugFeedbackTranslated("debug.reload_chunks.message");
    }

    public static void toggleChunkBorders() {
        //? if >=1.21.9 {
        boolean flag = mc.debugEntries.toggleStatus(DebugScreenEntries.CHUNK_BORDERS);
        //?} else {
        /*boolean flag = mc.debugRenderer.switchRenderChunkborder();
        *///?}

        debugFeedbackTranslated(flag ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
    }

    public static void toggleAdvancedTooltips() {
        boolean enabled = mc.options.advancedItemTooltips = !mc.options.advancedItemTooltips;
        mc.options.save();
        
        debugFeedbackTranslated(enabled ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
    }

    public static void toggleEntityHitboxes() {
        //? if >=1.21.9 {
        boolean flag = mc.debugEntries.toggleStatus(DebugScreenEntries.ENTITY_HITBOXES);
        //?} else {
        /*boolean flag = !mc.getEntityRenderDispatcher().shouldRenderHitBoxes();
        mc.getEntityRenderDispatcher().setRenderHitBoxes(flag);
        *///?}

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
        debugFeedbackComponent(Component.translatable(message, args));
    }
}
