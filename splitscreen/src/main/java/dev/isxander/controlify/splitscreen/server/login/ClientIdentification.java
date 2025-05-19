package dev.isxander.controlify.splitscreen.server.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public sealed interface ClientIdentification {
    record Controller(int subPlayerCount) implements ClientIdentification {
        public static final StreamCodec<FriendlyByteBuf, Controller> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                Controller::subPlayerCount,
                Controller::new
        );
    }

    record Pawn(UUID controllerUuid, byte[] hmac, int subPlayerIndex) implements ClientIdentification {
        public static final int HMAC_SIZE_BITS = 256;
        public static final int HMAC_SIZE_BYTES = HMAC_SIZE_BITS / 8;

        public static final StreamCodec<FriendlyByteBuf, Pawn> STREAM_CODEC = StreamCodec.composite(
                StreamCodec.of(
                        (buf, uuid) -> buf.writeUUID(uuid),
                        buf -> buf.readUUID()
                ),
                Pawn::controllerUuid,
                ByteBufCodecs.byteArray(HMAC_SIZE_BYTES),
                Pawn::hmac,
                ByteBufCodecs.VAR_INT,
                Pawn::subPlayerIndex,
                Pawn::new
        );

        @Override
        public @NotNull String toString() {
            return "Pawn{" +
                    "hmac=" + Hex.encodeHexString(hmac) +
                    '}';
        }
    }

    StreamCodec<FriendlyByteBuf, ClientIdentification> STREAM_CODEC =
            StreamCodec.of(
                    (buf, id) -> {
                        switch (id) {
                            case Controller c -> {
                                buf.writeByte(0);
                                Controller.STREAM_CODEC.encode(buf, c);
                            }
                            case Pawn p -> {
                                buf.writeByte(1);
                                Pawn.STREAM_CODEC.encode(buf, p);
                            }
                        }
                    },
                    buf -> {
                        byte type = buf.readByte();
                        return switch (type) {
                            case 0 -> Controller.STREAM_CODEC.decode(buf);
                            case 1 -> Pawn.STREAM_CODEC.decode(buf);
                            default -> throw new IllegalStateException("Unexpected value: " + type);
                        };
                    }
            );
}
