package dev.isxander.controlify.splitscreen.protocol;

import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.SplitscreenController;
import dev.isxander.controlify.splitscreen.protocol.packets.common.PawnboundDisconnectPacket;
import dev.isxander.controlify.splitscreen.protocol.packets.handshake.HandshakeProtocols;
import dev.isxander.controlify.splitscreen.protocol.packets.handshake.ControllerHandshakePacketListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

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

    public volatile boolean running;

    public ControllerConnectionListener() {
        this.running = true;
    }

    public void startTcpServerListener(int port, SplitscreenController master) throws IOException {
        synchronized (this.channels) {
            Class<? extends ServerSocketChannel> channelType;
            EventLoopGroup eventLoopGroup;
            if (Epoll.isAvailable()) {
                channelType = EpollServerSocketChannel.class;
                eventLoopGroup = SERVER_EPOLL_EVENT_GROUP.get();
            } else {
                channelType = NioServerSocketChannel.class;
                eventLoopGroup = SERVER_EVENT_GROUP.get();
            }

            this.channels.add(new ServerBootstrap().channel(channelType).childHandler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel ch) {
                    try {
                        ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                    } catch (ChannelException e) {
                    }

                    ChannelPipeline pipeline = ch.pipeline().addLast("timeout", new ReadTimeoutHandler(5));

                    ConnectionUtils.configureSerialization(pipeline, PacketFlow.SERVERBOUND, false, HandshakeProtocols.CONTROLLERBOUND);
                    Connection connection = new Connection(PacketFlow.SERVERBOUND);
                    ControllerConnectionListener.this.connections.add(connection);
                    connection.configurePacketHandler(pipeline);
                    connection.setListenerForServerboundHandshake(new ControllerHandshakePacketListener(master, connection));
                    LOGGER.info("Established connection with {}", ch.remoteAddress());
                }
            }).group(eventLoopGroup).localAddress(InetAddress.getLoopbackAddress(), port).bind().syncUninterruptibly());
        }
    }

    public void stop() {
        this.running = false;

        for (ChannelFuture channel : this.channels) {
            try {
                channel.channel().close().sync();
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted whilst closing channel");
            }
        }
    }

    public void tick() {
        synchronized (this.connections) {
            Iterator<Connection> iterator = this.connections.iterator();

            while (iterator.hasNext()) {
                Connection connection = iterator.next();

                if (!connection.isConnecting()) {
                    if (connection.isConnected()) {
                        try {
                            connection.tick();
                        } catch (Exception e) {
                            LOGGER.warn("Failed to handle packet for {}", connection.getLoggableAddress(false), e);
                            Component component = Component.literal("Internal server error");
                            connection.send(new PawnboundDisconnectPacket(), PacketSendListener.thenRun(() -> connection.disconnect(component)));
                            connection.setReadOnly();
                        }
                    } else {
                        iterator.remove();
                        connection.handleDisconnection();
                    }
                }
            }
        }
    }

    public List<Connection> getConnections() {
        return this.connections;
    }
}
