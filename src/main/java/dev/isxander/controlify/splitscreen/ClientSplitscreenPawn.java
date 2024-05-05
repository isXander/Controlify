package dev.isxander.controlify.splitscreen;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.mixins.feature.splitscreen.WindowAccessor;
import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.lwjgl.glfw.GLFW;

import java.net.InetAddress;

public class ClientSplitscreenPawn implements SplitscreenPawn {
    private static final Minecraft minecraft = Minecraft.getInstance();

    @Override
    public void joinMyServer(int port) {
        minecraft.execute(() -> {
            String host = InetAddress.getLoopbackAddress().getHostAddress();
            String ip = host + ":" + port;

            ConnectScreen.startConnecting(minecraft.screen, minecraft, new ServerAddress(host, port), new ServerData("Splitscreen Master", ip, ServerData.Type.LAN), false, null);
        });
    }

    @SuppressWarnings("UnreachableCode")
    @Override
    public void configureSplitscreen(long monitorIndex, SplitscreenPosition position) {
        minecraft.execute(() -> {
            Window window = minecraft.getWindow();
            ScreenManager screenManager = ((WindowAccessor) (Object) window).getScreenManager();
            Monitor monitor = screenManager.getMonitor(monitorIndex);
            VideoMode videoMode = monitor.getCurrentMode();

            boolean isHidden = position == SplitscreenPosition.HIDDEN;
            GLFW.glfwSetWindowAttrib(window.getWindow(), GLFW.GLFW_VISIBLE, isHidden ? GLFW.GLFW_FALSE : GLFW.GLFW_TRUE);

            GLFW.glfwSetWindowAttrib(window.getWindow(), GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);

            ScreenRectangle windowPosition = position.applyToRealDims(monitor.getX(), monitor.getY(), videoMode.getWidth(), videoMode.getHeight());

            GLFW.glfwSetWindowMonitor(
                    window.getWindow(),
                    0L,
                    windowPosition.left(), windowPosition.top(),
                    windowPosition.width(), windowPosition.height(),
                    GLFW.GLFW_DONT_CARE
            );
        });
    }
}
