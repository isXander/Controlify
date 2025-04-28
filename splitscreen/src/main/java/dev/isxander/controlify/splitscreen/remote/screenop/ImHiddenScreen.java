package dev.isxander.controlify.splitscreen.remote.screenop;

import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.controlify.splitscreen.screenop.ScreenSplitscreenMode;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.network.chat.Component;

public class ImHiddenScreen extends GenericMessageScreen implements ScreenSplitscreenBehaviour {
    public ImHiddenScreen() {
        super(Component.literal("This Minecraft window is currently being hidden as splitscreen is in fullscreen mode and this is not the host client. If you see this, please report it as a bug."));
    }

    @Override
    public void added() {
        if (SplitscreenBootstrapper.getController().isPresent()) {
            throw new IllegalStateException("ImHiddenScreen should not be created on the host client.");
        }
    }

    @Override
    public ScreenSplitscreenMode controlify$splitscreen$getMode() {
        return ScreenSplitscreenMode.SPLITSCREEN;
    }
}
