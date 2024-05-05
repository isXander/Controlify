package dev.isxander.controlify.splitscreen;

import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.splitscreen.protocol.ControllerConnectionListener;
import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.util.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SplitscreenController {
    private final List<SplitscreenPawn> pawns = new ArrayList<>();
    private final ControllerConnectionListener connectionListener;
    private int port;

    public SplitscreenController() {
        this.pawns.add(new ControllerSplitscreenPawn()); // add this client as a slave of itself
        this.connectionListener = new ControllerConnectionListener();
    }

    public void applyToPawns(Consumer<SplitscreenPawn> consumer) {
        pawns.forEach(consumer);
    }

    public void negotiateSplitscreen() {
        int windowCount = this.pawns.size();

        Window window = Minecraft.getInstance().getWindow();
        long monitor = window.findBestMonitor().getMonitor();

        switch (windowCount) {
            case 1 -> {} // don't do anything
            case 2 -> {
                this.pawns.get(0).configureSplitscreen(monitor, SplitscreenPosition.LEFT);
                this.pawns.get(1).configureSplitscreen(monitor, SplitscreenPosition.RIGHT);
            }
            case 3 -> {
                this.pawns.get(0).configureSplitscreen(monitor, SplitscreenPosition.LEFT);
                this.pawns.get(1).configureSplitscreen(monitor, SplitscreenPosition.TOP_RIGHT);
                this.pawns.get(2).configureSplitscreen(monitor, SplitscreenPosition.BOTTOM_RIGHT);
            }
            case 4 -> {
                this.pawns.get(0).configureSplitscreen(monitor, SplitscreenPosition.TOP_LEFT);
                this.pawns.get(1).configureSplitscreen(monitor, SplitscreenPosition.TOP_RIGHT);
                this.pawns.get(2).configureSplitscreen(monitor, SplitscreenPosition.BOTTOM_LEFT);
                this.pawns.get(3).configureSplitscreen(monitor, SplitscreenPosition.BOTTOM_RIGHT);
            }
            default -> throw new IllegalStateException("Do not know how to organise " + windowCount + " monitors!");
        }
    }

    public void addPawn(SplitscreenPawn slave) {
        this.pawns.add(slave);
        this.negotiateSplitscreen();
    }

    public void removePawn(SplitscreenPawn slave) {
        this.pawns.remove(slave);
        this.negotiateSplitscreen();
    }

    public void startServer() {
        try {
            this.port = HttpUtil.getAvailablePort();
            System.out.println("Opening on port " + this.port);
            this.connectionListener.startTcpServerListener(this.port, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void tick() {
        this.connectionListener.tick();
    }
}
