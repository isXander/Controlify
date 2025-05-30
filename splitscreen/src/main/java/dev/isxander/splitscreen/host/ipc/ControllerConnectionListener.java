package dev.isxander.splitscreen.host.ipc;

import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import dev.isxander.splitscreen.ipc.IPCMethod;
import dev.isxander.splitscreen.ipc.ConnectionUtils;
import dev.isxander.splitscreen.host.SplitscreenController;
import dev.isxander.splitscreen.ipc.SplitscreenConnection;
import dev.isxander.splitscreen.ipc.packets.pawnbound.common.PawnboundDisconnectPacket;
import dev.isxander.splitscreen.ipc.packets.HandshakeProtocols;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerDomainSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnixDomainSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Listens for incoming connections from pawns.
 */
public class ControllerConnectionListener {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Supplier<EpollEventLoopGroup> SERVER_EPOLL_EVENT_GROUP = Suppliers.memoize(
            () -> new EpollEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("Controlify netty epoll server IO #%d").setDaemon(true).build())
    );

    public static final Supplier<NioEventLoopGroup> SERVER_EVENT_GROUP = Suppliers.memoize(
            () -> new NioEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("Controlify netty epoll server IO #%d").setDaemon(true).build())
    );

    private final List<ChannelFuture> channels = Collections.synchronizedList(new ArrayList<>());
    private final List<Connection> connections = Collections.synchronizedList(new ArrayList<>());

    private final Minecraft minecraft;

    private volatile boolean running;

    public ControllerConnectionListener(IPCMethod ipcMethod, SplitscreenController controller, Minecraft minecraft) {
        this.running = true;
        this.minecraft = minecraft;
        switch (ipcMethod) {
            case IPCMethod.TCP(int port) -> startTcpListener(port, controller);
            case IPCMethod.Unix(String socketPath) -> startUnixListener(socketPath, controller);
        }
    }

    private void startUnixListener(String socketPath, SplitscreenController controller) {
        LOGGER.info("Starting unix socket listener on {}", socketPath);

        Path socketPathFile = Path.of(socketPath);
        try {
            Files.createDirectories(socketPathFile.getParent());
            Files.deleteIfExists(socketPathFile);
        } catch (IOException e) {
            LOGGER.error("Failed to cleanup socket path", e);
        }

        startListener(controller, new ServerBootstrap()
                .channel(Epoll.isAvailable() ? EpollServerDomainSocketChannel.class : NioServerDomainSocketChannel.class)
                .localAddress(Epoll.isAvailable() ? new DomainSocketAddress(socketPath) : UnixDomainSocketAddress.of(socketPathFile)));
    }

    private void startTcpListener(int port, SplitscreenController controller) {
        LOGGER.info("Starting tcp listener on port {}", port);

        startListener(controller, new ServerBootstrap()
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .localAddress(InetAddress.getLoopbackAddress(), port));
    }

    private void startListener(SplitscreenController controller, ServerBootstrap boostrap) {
        synchronized (this.channels) {
            boostrap
                    .group(Epoll.isAvailable() ? SERVER_EPOLL_EVENT_GROUP.get() : SERVER_EVENT_GROUP.get())
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            try {
                                ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                            } catch (ChannelException ignored) {}

                            ChannelPipeline pipeline = ch.pipeline()
                                    .addLast("timeout", new ReadTimeoutHandler(5));

                            ConnectionUtils.configureSerialization(pipeline, PacketFlow.SERVERBOUND, false, HandshakeProtocols.CONTROLLERBOUND);
                            Connection connection = new SplitscreenConnection(PacketFlow.SERVERBOUND);
                            ControllerConnectionListener.this.connections.add(connection);
                            connection.configurePacketHandler(pipeline);
                            connection.setListenerForServerboundHandshake(new ControllerHandshakePacketListener(controller, connection, minecraft));
                            LOGGER.info("Established connection with {}", ch.remoteAddress());
                        }
                    });

            this.channels.add(
                    boostrap.bind().syncUninterruptibly()
            );
        }
    }

    public void stop() {
        this.running = false;

        for (ChannelFuture channel : this.channels) {
            try {
                channel.channel().close().sync();
            } catch (InterruptedException e) {
                LOGGER.error("Failed to close channel", e);
            }
        }
    }

    public void tick() {
        synchronized (this.connections) {
            Iterator<Connection> it = this.connections.iterator();

            while (it.hasNext()) {
                Connection connection = it.next();

                if (connection.isConnecting())
                    continue;

                if (connection.isConnected()) {
                    try {
                        connection.tick();
                    } catch (Throwable e) {
                        LOGGER.error("Failed to handle packet for {}", connection.getLoggableAddress(false), e);
                        Component component = Component.literal("Internal server error");
                        connection.send(new PawnboundDisconnectPacket(component), PacketSendListener.thenRun(() -> connection.disconnect(component)));
                        connection.setReadOnly();
                    }
                } else {
                    LOGGER.info("Disconnected {}", connection.getLoggableAddress(false));

                    it.remove();
                    connection.handleDisconnection();
                }
            }
        }
    }

    public List<Connection> getConnections() {
        return Collections.unmodifiableList(this.connections);
    }
}
