package dev.isxander.controlify.splitscreen.mixins.screenop;

import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Screen.class)
public abstract class ScreenMixin implements ScreenSplitscreenBehaviour {

    // Aka does this screen stop time in singleplayer
    @Shadow
    public abstract boolean isPauseScreen();

    @Override
    public ScreenSplitscreenMode controlify$splitscreen$getMode() {
        // An initial hint to aid a tad in mod compatibility. The pause screen itself is also splitscreen.
        return this.isPauseScreen() ? ScreenSplitscreenMode.FULLSCREEN : ScreenSplitscreenMode.SPLITSCREEN;
    }
}
