package dev.isxander.controlify.splitscreen.client.protocol;

import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.SocketConnectionMethod;
import dev.isxander.controlify.splitscreen.protocol.ConnectionUtils;
import dev.isxander.controlify.splitscreen.client.protocol.handshake.ControllerboundHandshakePacket;
import dev.isxander.controlify.splitscreen.protocol.HandshakeProtocols;
import dev.isxander.controlify.splitscreen.client.protocol.play.ControllerboundHelloPacket;
import dev.isxander.controlify.splitscreen.client.protocol.play.PawnPlayPacketListener;
import dev.isxander.controlify.splitscreen.protocol.PlayProtocols;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerDomainSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.util.function.Supplier;

public class PawnConnectionListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Supplier<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = Suppliers.memoize(
            () -> new EpollEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("Controlify Netty Epoll Client IO #%d").setDaemon(true).build())
    );
    private static final Supplier<NioEventLoopGroup> NETWORK_WORKER_GROUP = Suppliers.memoize(
            () -> new NioEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("Controlify Netty Client IO #%d").setDaemon(true).build())
    );

    private final Connection controllerConnection;

    public PawnConnectionListener(Minecraft minecraft, SocketConnectionMethod connectionMethod) {
        this.controllerConnection = switch (connectionMethod) {
            case SocketConnectionMethod.TCP(int port) -> connectToTcp(port, minecraft);
            case SocketConnectionMethod.Unix(String socketPath) -> connectToUnixSocket(socketPath, minecraft);
        };
    }

    public Connection getControllerConnection() {
        return controllerConnection;
    }

    private Connection connectToUnixSocket(String socketPath, Minecraft minecraft) {
        LOGGER.info("Connecting to controller unix socket at {}", socketPath);

        return connect(minecraft, new Bootstrap()
                .channel(Epoll.isAvailable() ? EpollDomainSocketChannel.class : NioServerDomainSocketChannel.class)
                .remoteAddress(new DomainSocketAddress(socketPath)));
    }

    private Connection connectToTcp(int port, Minecraft minecraft) {
        LOGGER.info("Connecting to controller tcp port {}", port);

        return connect(minecraft, new Bootstrap()
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .remoteAddress(InetAddress.getLoopbackAddress(), port));
    }

    private Connection connect(Minecraft minecraft, Bootstrap bootstrap) {
        Validate.isTrue(controllerConnection == null, "Already connected to a controller");

        Connection connection = new Connection(PacketFlow.CLIENTBOUND);

        bootstrap
                .group(Epoll.isAvailable() ? NETWORK_EPOLL_WORKER_GROUP.get() : NETWORK_WORKER_GROUP.get())
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        try {
                            ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                        } catch (ChannelException ignored) {
                        }

                        ChannelPipeline pipeline = ch.pipeline()
                                .addLast("timeout", new ReadTimeoutHandler(5));
                        ConnectionUtils.configureSerialization(pipeline, PacketFlow.CLIENTBOUND, false, HandshakeProtocols.CONTROLLERBOUND);
                        connection.configurePacketHandler(pipeline);

                        LOGGER.info("Established connection with controller");
                    }
                }).connect().syncUninterruptibly();

        connection.runOnceConnected(c -> {
            c.setupInboundProtocol(PlayProtocols.PAWNBOUND, new PawnPlayPacketListener(c, minecraft));
            c.send(new ControllerboundHandshakePacket(1), null, true);
            c.setupOutboundProtocol(PlayProtocols.CONTROLLERBOUND);
        });
        // will run after above since it is flushed above
        // TODO: too early to access window handle, it hasn't been created yet, have to negotiate window later
        connection.send(new ControllerboundHelloPacket(0L));

        return connection;
    }
}
