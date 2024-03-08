package dev.isxander.controlify.hid;

import java.util.HexFormat;

public record HIDIdentifier(int vendorId, int productId) {
    @Override
    public String toString() {
        var hex = HexFormat.of();
        return "HID[" +
                "VID=0x" + hex.toHexDigits(vendorId, 4) +
                ", PID=0x" + hex.toHexDigits(productId, 4) +
                ']';
    }
}
