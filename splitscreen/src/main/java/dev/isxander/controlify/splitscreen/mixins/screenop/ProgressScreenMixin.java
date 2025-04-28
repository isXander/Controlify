package dev.isxander.controlify.splitscreen.mixins.screenop;

import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import net.minecraft.client.gui.screens.ProgressScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ProgressScreen.class)
public class ProgressScreenMixin implements ScreenSplitscreenBehaviour {
    @Override
    public ScreenSplitscreenMode controlify$splitscreen$getMode() {
        return ScreenSplitscreenMode.SPLITSCREEN;
    }
}
