package dev.isxander.controlify.splitscreen.mixins.screenop;

import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DisconnectedScreen.class)
public class DisconnectScreenMixin implements ScreenSplitscreenBehaviour {
    @Override
    public ScreenSplitscreenMode controlify$splitscreen$getMode() {
        // not technically a splitscreen screen, but
        // TODO: propagate disconnection errors to controller and disconnect everyone
        return ScreenSplitscreenMode.SPLITSCREEN;
    }
}
