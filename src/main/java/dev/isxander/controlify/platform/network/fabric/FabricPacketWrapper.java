//? if fabric {
package dev.isxander.controlify.platform.network.fabric;

import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/*? if >1.20.4 {*/
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/*?} else {*/
/*import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
*//*?}*/

public class FabricPacketWrapper<T> {
    private final ResourceLocation channel;
    private final ControlifyPacketCodec<T> codec;

    /*? if >1.20.4 {*/
    public final CustomPacketPayload.Type<FabricPacketPayloadWrapper> type;
    private final StreamCodec<FriendlyByteBuf, FabricPacketPayloadWrapper> streamCodec;
    /*?} else {*/
    /*public final PacketType<FabricPacketPayloadWrapper> type;
    *//*?}*/

    public FabricPacketWrapper(
            ResourceLocation channel,
            ControlifyPacketCodec<T> codec/*? if >1.20.4 {*/,
            PayloadTypeRegistry<? extends FriendlyByteBuf> registry
            /*?}*/
    ) {
        this.channel = channel;
        this.codec = codec;

        /*? if >1.20.4 {*/
        this.type = new CustomPacketPayload.Type<>(channel);
        this.streamCodec = StreamCodec.of(
                (buf, wrapper) -> codec.encode(buf, wrapper.payload),
                buf -> new FabricPacketPayloadWrapper(codec.decode(buf))
        );

        registry.register(type, streamCodec);
        /*?} else {*/
        /*this.type = PacketType.create(channel, buf -> new FabricPacketPayloadWrapper(codec.decode(buf)));
        *//*?}*/
    }

    public class FabricPacketPayloadWrapper implements /*? if >1.20.4 {*/ CustomPacketPayload /*?} else {*/ /*FabricPacket *//*?}*/ {
        public final T payload;

        public FabricPacketPayloadWrapper(T payload) {
            this.payload = payload;
        }

        /*? if <=1.20.4 {*/
        /*@Override
        public void write(FriendlyByteBuf buf) {
            codec.encode(buf, payload);
        }

        @Override
        public PacketType<?> getType() {
            return type;
        }
        *//*?}*/

        /*? if >1.20.4 {*/
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return type;
        }
        /*?}*/
    }
}
//?}
