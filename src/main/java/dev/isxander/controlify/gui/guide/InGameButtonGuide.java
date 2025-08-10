package dev.isxander.controlify.gui.guide;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.api.guide.InGameCtx;
import dev.isxander.controlify.controller.GenericControllerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class InGameButtonGuide {
    private final ControllerEntity controller;
    private final Minecraft minecraft;

    public InGameButtonGuide(ControllerEntity controller, Minecraft minecraft) {
        this.controller = controller;
        this.minecraft = minecraft;
    }

    public void renderHud(GuiGraphics graphics, float tickDelta) {
        boolean debugOpen = minecraft.getDebugOverlay().showDebugScreen();
        boolean hideGui = minecraft.options.hideGui;
        GenericControllerConfig config = controller.genericConfig().config();

        if (!debugOpen && !hideGui && config.showIngameGuide) {
            GuideRenderer.render(graphics, GuideDomains.IN_GAME, minecraft, config.ingameGuideBottom, true);
        }
    }

    public void tick() {
        GenericControllerConfig config = controller.genericConfig().config();

        if (config.showIngameGuide) {
            if (minecraft.hitResult == null) {
                minecraft.gameRenderer.pick(1f);
            }
            GuideDomains.IN_GAME.updateGuides(new InGameCtx(minecraft, minecraft.player, minecraft.level, minecraft.hitResult, controller, config.guideVerbosity), minecraft.font);
        }
    }
}
