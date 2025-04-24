package dev.isxander.controlify.splitscreen.ipc;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.flow.FlowControlHandler;
import net.minecraft.network.*;
import net.minecraft.network.protocol.PacketFlow;

public class ConnectionUtils {
    /**
     * Utility method that replicates
     * {@link net.minecraft.network.Connection#configureSerialization(ChannelPipeline, PacketFlow, boolean, BandwidthDebugMonitor)}
     * but where a custom initial protocol can be set.
     */
    public static void configureSerialization(ChannelPipeline pipeline, PacketFlow inbound, boolean memoryOnly, ProtocolInfo<?> initialProtocol) {
        PacketFlow outbound = inbound.getOpposite();
        boolean decoder = inbound == PacketFlow.SERVERBOUND;
        boolean encoder = outbound == PacketFlow.SERVERBOUND;

        pipeline.addLast("splitter", createFrameDecoder(memoryOnly))
                .addLast(new FlowControlHandler())
                .addLast(inboundHandlerName(decoder), decoder ? new PacketDecoder<>(initialProtocol) : new UnconfiguredPipelineHandler.Inbound())
                .addLast("prepender", createFrameEncoder(memoryOnly))
                .addLast(outboundHandlerName(encoder), encoder ? new PacketEncoder<>(initialProtocol) : new UnconfiguredPipelineHandler.Outbound());
    }

    public static ChannelOutboundHandler createFrameEncoder(boolean memoryOnly) {
        return memoryOnly ? new LocalFrameEncoder() : new Varint21LengthFieldPrepender();
    }

    public static ChannelInboundHandler createFrameDecoder(boolean memoryOnly) {
        return memoryOnly ? new LocalFrameDecoder() : new Varint21FrameDecoder(null);
    }

    public static String inboundHandlerName(boolean serverbound) {
        return serverbound ? "decoder" : "inbound_config";
    }

    public static String outboundHandlerName(boolean clientbound) {
        return clientbound ? "encoder" : "outbound_config";
    }
}
