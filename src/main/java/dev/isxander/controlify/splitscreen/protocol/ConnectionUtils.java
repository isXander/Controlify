package dev.isxander.controlify.splitscreen.protocol;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.flow.FlowControlHandler;
import net.minecraft.network.*;
import net.minecraft.network.protocol.PacketFlow;

public class ConnectionUtils {
    /**
     * Utility method that replicates
     * {@link Connection#configureSerialization(ChannelPipeline, PacketFlow, boolean, BandwidthDebugMonitor)}
     * but where a custom initial protocol can be set.
     */
    public static void configureSerialization(ChannelPipeline pipeline, PacketFlow inbound, boolean noop, ProtocolInfo<?> initialProtocol) {
        PacketFlow outbound = inbound.getOpposite();
        boolean decoder = inbound == PacketFlow.SERVERBOUND;
        boolean encoder = outbound == PacketFlow.SERVERBOUND;

        pipeline.addLast("splitter", createFrameDecoder(noop))
                .addLast(new FlowControlHandler())
                .addLast(inboundHandlerName(decoder), decoder ? new PacketDecoder<>(initialProtocol) : new UnconfiguredPipelineHandler.Inbound())
                .addLast("prepender", createFrameEncoder(noop))
                .addLast(outboundHandlerName(encoder), encoder ? new PacketEncoder<>(initialProtocol) : new UnconfiguredPipelineHandler.Outbound());

    }

    public static ChannelOutboundHandler createFrameEncoder(boolean noop) {
        return noop ? new NoOpFrameEncoder() : new Varint21LengthFieldPrepender();
    }

    public static ChannelInboundHandler createFrameDecoder(boolean noop) {
        return noop ? new NoOpFrameDecoder() : new Varint21FrameDecoder(null);
    }


    public static String inboundHandlerName(boolean serverbound) {
        return serverbound ? "decoder" : "inbound_config";
    }

    public static String outboundHandlerName(boolean clientbound) {
        return clientbound ? "encoder" : "outbound_config";
    }
}
