package dev.isxander.controlify.splitscreen.protocol;

import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.isxander.controlify.splitscreen.protocol.packets.handshake.HandshakeProtocols;
import dev.isxander.controlify.splitscreen.protocol.packets.handshake.ControllerboundHandshakePacket;
import dev.isxander.controlify.splitscreen.protocol.packets.play.ControllerboundHelloPacket;
import dev.isxander.controlify.splitscreen.protocol.packets.play.PlayProtocols;
import dev.isxander.controlify.splitscreen.protocol.packets.play.PawnPlayPacketListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.apache.commons.lang3.Validate;

import java.net.InetAddress;
import java.util.function.Supplier;

public class PawnConnectionManager {
    private static final Supplier<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = Suppliers.memoize(
            () -> new EpollEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("Controlify Netty Epoll Client IO #%d").setDaemon(true).build())
    );
    private static final Supplier<NioEventLoopGroup> NETWORK_WORKER_GROUP = Suppliers.memoize(
            () -> new NioEventLoopGroup(2, new ThreadFactoryBuilder().setNameFormat("Controlify Netty Client IO #%d").setDaemon(true).build())
    );

    private Connection controllerConnection;

    public void connect(int port) {
        Validate.isTrue(controllerConnection == null, "Tried to connect to controller when there already is one.");

        Connection connection = new Connection(PacketFlow.CLIENTBOUND);

        Class<? extends SocketChannel> channelType;
        EventLoopGroup eventLoopGroup;
        if (Epoll.isAvailable()) {
            channelType = EpollSocketChannel.class;
            eventLoopGroup = NETWORK_EPOLL_WORKER_GROUP.get();
        } else {
            channelType = NioSocketChannel.class;
            eventLoopGroup = NETWORK_WORKER_GROUP.get();
        }

        ChannelFuture channelFuture = new Bootstrap().group(eventLoopGroup).handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                try {
                    ch.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException e) {
                }

                ChannelPipeline pipeline = ch.pipeline().addLast("timeout", new ReadTimeoutHandler(5));
                ConnectionUtils.configureSerialization(pipeline, PacketFlow.CLIENTBOUND, false, HandshakeProtocols.CONTROLLERBOUND);
                connection.configurePacketHandler(pipeline);
            }
        }).channel(channelType).connect(InetAddress.getLoopbackAddress(), port);

        channelFuture.syncUninterruptibly();

        this.controllerConnection = connection;

        this.controllerConnection.runOnceConnected(c -> {
            c.setupInboundProtocol(PlayProtocols.PAWNBOUND, new PawnPlayPacketListener(c));
            c.sendPacket(new ControllerboundHandshakePacket(SplitscreenProtocol.VERSION), null, true);
            c.setupOutboundProtocol(PlayProtocols.CONTROLLERBOUND);
        });

        this.controllerConnection.send(ControllerboundHelloPacket.INSTANCE);
    }

    public Connection getConnection() {
        return this.controllerConnection;
    }
}
