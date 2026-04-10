package dev.isxander.controlify.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;

public final class DebugOverlayHelper {
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isOverlayEnabled() {
        return mc.debugEntries.isOverlayVisible();
    }

    public static void toggleOverlay() {
        mc.debugEntries.toggleDebugOverlay();
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
        boolean flag = mc.debugEntries.toggleStatus(DebugScreenEntries.CHUNK_BORDERS);

        debugFeedbackTranslated(flag ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
    }

    public static void toggleAdvancedTooltips() {
        boolean enabled = mc.options.advancedItemTooltips = !mc.options.advancedItemTooltips;
        mc.options.save();
        
        debugFeedbackTranslated(enabled ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
    }

    public static void toggleEntityHitboxes() {
        boolean flag = mc.debugEntries.toggleStatus(DebugScreenEntries.ENTITY_HITBOXES);

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

    private static ChatComponent getChat() {
        //? if >=26.2 {
        return mc.gui.hud.getChat();
        //?} else {
        /*return mc.gui.getChat();
         *///?}
    }

    public static void clearChat() {
        getChat().clearMessages(false);
    }

    private static void debugComponent(ChatFormatting formatting, Component message) {
        getChat()
                .addClientSystemMessage(
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
