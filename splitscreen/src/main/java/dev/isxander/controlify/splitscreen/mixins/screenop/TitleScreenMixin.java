package dev.isxander.controlify.splitscreen.mixins.screenop;

import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TitleScreen.class)
public class TitleScreenMixin implements ScreenSplitscreenBehaviour {
    @Override
    public ScreenSplitscreenMode controlify$splitscreen$getMode() {
        return ScreenSplitscreenMode.FULLSCREEN;
    }
}
