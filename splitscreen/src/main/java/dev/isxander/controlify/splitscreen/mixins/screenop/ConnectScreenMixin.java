package dev.isxander.controlify.splitscreen.mixins.screenop;

import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import net.minecraft.client.gui.screens.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin implements ScreenSplitscreenBehaviour {
    @Override
    public ScreenSplitscreenMode controlify$splitscreen$getMode() {
        return ScreenSplitscreenMode.SPLITSCREEN;
    }
}
