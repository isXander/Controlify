package dev.isxander.controlify.splitscreen.server;

import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.server.protocol.play.PawnboundJoinServerPacket;
import net.minecraft.network.Connection;

public class ServerSplitscreenPawn implements SplitscreenPawn {
    private final Connection connection;

    public ServerSplitscreenPawn(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void joinServer(String serverAddress, int serverPort) {
        this.connection.send(new PawnboundJoinServerPacket(serverAddress, serverPort));
    }
}
