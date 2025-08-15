package dev.isxander.controlify.hid;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.utils.codec.CExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.HexFormat;

public record HIDIdentifier(int vendorId, int productId) {
    public static final Codec<HIDIdentifier> CODEC = CExtraCodecs.arrayPair(
            Codec.INT,
            HIDIdentifier::vendorId, HIDIdentifier::productId,
            HIDIdentifier::new
    );

    @Override
    public @NotNull String toString() {
        var hex = HexFormat.of();
        return "HID[" +
                "VID=0x" + hex.toHexDigits(vendorId, 4) +
                ", PID=0x" + hex.toHexDigits(productId, 4) +
                ']';
    }
}
