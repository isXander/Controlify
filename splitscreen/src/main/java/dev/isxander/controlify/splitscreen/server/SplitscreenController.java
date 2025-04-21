package dev.isxander.controlify.splitscreen.server;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.SocketConnectionMethod;
import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.client.ClientSplitscreenPawn;
import dev.isxander.controlify.splitscreen.server.protocol.ControllerConnectionListener;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SplitscreenController {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<SplitscreenPawn> pawns = new ArrayList<>();
    private final ControllerConnectionListener connectionListener;

    public SplitscreenController(Minecraft minecraft, SocketConnectionMethod connectionMethod) {
        this.connectionListener = new ControllerConnectionListener(connectionMethod, this);
        this.pawns.add(new ClientSplitscreenPawn(minecraft)); // control ourselves as a pawn
    }

    public void forEachPawn(Consumer<SplitscreenPawn> consumer) {
        pawns.forEach(consumer);
    }

    public void addPawn(SplitscreenPawn pawn) {
        LOGGER.info("Adding pawn #{}", this.pawns.size());

        this.pawns.add(pawn);
    }

    public void removePawn(SplitscreenPawn pawn) {
        this.pawns.remove(pawn);
    }

    public void setupParentWindow() {
        long windowHandle = GLFW.glfwCreateWindow(1920, 1080, "Controlify Splitscreen Parent", 0, 0);
        if (windowHandle == 0) {
            throw new RuntimeException("Failed to create GLFW window");
        }
    }

    public void negotiateSplitscreen() {

    }
}
