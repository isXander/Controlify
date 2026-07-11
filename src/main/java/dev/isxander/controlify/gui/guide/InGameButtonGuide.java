package dev.isxander.controlify.gui.guide;

import dev.isxander.controlify.api.guide.GuideInstance;
import dev.isxander.controlify.config.settings.profile.GenericControllerSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.api.guide.InGameCtx;
import dev.isxander.controlify.mixins.feature.guide.ingame.MinecraftAccessor;
import dev.isxander.controlify.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class InGameButtonGuide {
    private final ControllerEntity controller;
    private final Minecraft minecraft;
    private final GuideInstance<InGameCtx> guideInstance;

    public InGameButtonGuide(ControllerEntity controller, Minecraft minecraft) {
        this.controller = controller;
        this.minecraft = minecraft;
        this.guideInstance = GuideDomains.IN_GAME.createInstance();
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta) {
        boolean debugOpen = minecraft.getDebugOverlay().showDebugScreen();
        //? if >=26.2 {
        boolean hideGui = minecraft.gui.hud.isHidden();
        //?} else {
        /*boolean hideGui = minecraft.options.hideGui;
        *///?}
        boolean screenOpen = MinecraftUtil.getScreen() != null;
        GenericControllerSettings.GuideSettings settings = controller.settings().generic.guide;

        if (!debugOpen && !hideGui && !screenOpen && settings.showIngameGuide) {
            this.guideInstance.extractRenderState(graphics, settings.ingameGuideBottom, true);
        }
    }

    public void tick() {
        GenericControllerSettings.GuideSettings settings = controller.settings().generic.guide;

        if (settings.showIngameGuide) {
            if (minecraft.hitResult == null) {
                ((MinecraftAccessor) minecraft).controlify$invokePick(1f);
            }
            this.guideInstance.update(
                    new InGameCtx(
                            minecraft,
                            minecraft.player,
                            minecraft.level,
                            minecraft.hitResult,
                            controller,
                            settings.verbosity
                    ),
                    minecraft.font
            );
        }
    }
}
