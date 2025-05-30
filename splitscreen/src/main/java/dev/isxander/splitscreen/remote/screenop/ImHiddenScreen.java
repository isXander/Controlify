package dev.isxander.splitscreen.remote.screenop;

import dev.isxander.splitscreen.SplitscreenBootstrapper;
import dev.isxander.splitscreen.screenop.ScreenSplitscreenBehaviour;
import dev.isxander.splitscreen.screenop.ScreenSplitscreenMode;
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
    public ScreenSplitscreenMode getSplitscreenMode() {
        return ScreenSplitscreenMode.SPLITSCREEN;
    }
}
