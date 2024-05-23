package dev.isxander.controlify.mixins.feature.guide.ingame;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.gui.guide.InGameButtonGuide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void tickButtonGuide(CallbackInfo ci) {
        if (minecraft.level == null) return;

        Controlify.instance().inGameButtonGuide().ifPresent(InGameButtonGuide::tick);
    }
}
