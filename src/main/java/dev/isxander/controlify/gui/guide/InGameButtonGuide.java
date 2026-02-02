package dev.isxander.controlify.gui.guide;

import dev.isxander.controlify.config.settings.profile.GenericControllerSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.api.guide.InGameCtx;
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
        boolean screenOpen = minecraft.screen != null;
        GenericControllerSettings.GuideSettings settings = controller.settings().generic.guide;

        if (!debugOpen && !hideGui && !screenOpen && settings.showIngameGuide) {
            GuideRenderer.render(graphics, GuideDomains.IN_GAME, minecraft, settings.ingameGuideButtom, true);
        }
    }

    public void tick() {
        GenericControllerSettings.GuideSettings settings = controller.settings().generic.guide;

        if (settings.showIngameGuide) {
            if (minecraft.hitResult == null) {
                minecraft.gameRenderer.pick(1f);
            }
            GuideDomains.IN_GAME.updateGuides(new InGameCtx(minecraft, minecraft.player, minecraft.level, minecraft.hitResult, controller, settings.verbosity), minecraft.font);
        }
    }
}
