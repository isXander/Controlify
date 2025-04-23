package dev.isxander.controlify.splitscreen.server;

import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.protocol.pawnbound.play.PawnboundJoinServerPacket;
import dev.isxander.controlify.splitscreen.protocol.pawnbound.play.PawnboundParentWindowPacket;
import dev.isxander.controlify.splitscreen.protocol.pawnbound.play.PawnboundSplitscreenPositionPacket;
import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;
import dev.isxander.controlify.splitscreen.window.manager.NativeWindowHandle;
import net.minecraft.network.Connection;

/**
 * A splitscreen pawn object that remotely controls another client using packets.
 */
public class RemoteSplitscreenPawn implements SplitscreenPawn {
    private final Connection connection;

    private SplitscreenPosition position = null;

    /**
     * Creates a new handle to a remote splitscreen pawn.
     * @param connection connection to a remote client
     */
    public RemoteSplitscreenPawn(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void joinServer(String serverAddress, int serverPort) {
        this.connection.send(new PawnboundJoinServerPacket(serverAddress, serverPort));
    }

    @Override
    public void setupWindowParent(NativeWindowHandle parentWindow, int x, int y, int width, int height) {
        this.connection.send(
                new PawnboundParentWindowPacket(
                        parentWindow,
                        x, y,
                        width, height
                )
        );
    }

    @Override
    public void setWindowSplitscreenMode(SplitscreenPosition position, int parentWidth, int parentHeight) {
        this.connection.send(new PawnboundSplitscreenPositionPacket(parentWidth, parentHeight, position));
        this.position = position;
    }

    @Override
    public SplitscreenPosition getWindowSplitscreenMode() {
        return this.position;
    }
}
