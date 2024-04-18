package dev.isxander.controlify.hid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.HexFormat;
import java.util.List;

public record HIDIdentifier(int vendorId, int productId) {
    public static final Codec<HIDIdentifier> LIST_CODEC = Codec.list(Codec.INT).comapFlatMap(
            parts -> {
                if (parts.size() != 2) {
                    return DataResult.error(() -> "HID identifier list must have exactly two elements, found " + parts.size());
                }
                return DataResult.success(new HIDIdentifier(parts.get(0), parts.get(1)));
            },
            hid -> List.of(hid.vendorId(), hid.productId())
    );

    @Override
    public String toString() {
        var hex = HexFormat.of();
        return "HID[" +
                "VID=0x" + hex.toHexDigits(vendorId, 4) +
                ", PID=0x" + hex.toHexDigits(productId, 4) +
                ']';
    }
}
