package dev.isxander.controlify.splitscreen;

import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import dev.isxander.controlify.splitscreen.window.manager.NativeWindowHandle;
import dev.isxander.controlify.splitscreen.window.manager.WindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.lwjgl.glfw.GLFW;

/**
 * A splitscreen pawn object that actually executes and controls a client.
 * Even the controller client is a pawn, and controls itself via this class' abstraction.
 */
public class LocalSplitscreenPawn implements SplitscreenPawn {
    private final Minecraft minecraft;

    private SplitscreenPosition position = null;

    public LocalSplitscreenPawn(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void joinServer(String host, int port) {
        String ip = host + ":" + port;
        var address = new ServerAddress(ip, port);
        var data = new ServerData("Splitscreen Master", ip, ServerData.Type.LAN);

        ConnectScreen.startConnecting(minecraft.screen, minecraft, address, data, false, null);
    }

    @Override
    public void setupWindowParent(NativeWindowHandle parentWindow, int x, int y, int width, int height) {
        WindowManager.get().embedThisWindow(parentWindow, x, y, width, height);
    }

    @Override
    public void setWindowSplitscreenMode(SplitscreenPosition position, int parentWidth, int parentHeight) {
        ScreenRectangle windowDims = position.applyToRealDims(0, 0, parentWidth, parentHeight);

        long windowHandle = minecraft.getWindow().getWindow();
        GLFW.glfwSetWindowPos(windowHandle, windowDims.left(), windowDims.top());
        GLFW.glfwSetWindowSize(windowHandle, windowDims.width(), windowDims.height());

        this.position = position;
    }

    @Override
    public void setWindowFocusState(boolean focused) {
        this.minecraft.setWindowActive(focused);
    }

    @Override
    public void closeGame() {
        this.minecraft.stop();
    }

    @Override
    public SplitscreenPosition getWindowSplitscreenMode() {
        return position;
    }
}
