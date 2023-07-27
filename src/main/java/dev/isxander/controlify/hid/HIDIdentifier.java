package dev.isxander.controlify.hid;

import java.util.HexFormat;

public record HIDIdentifier(int vendorId, int productId) {
    @Override
    public String toString() {
        var hex = HexFormat.of().withPrefix("0x");
        return "HIDIdentifier[" +
                "vendorId=" + hex.toHexDigits(vendorId) +
                ", productId=" + hex.toHexDigits(productId) +
                ']';
    }
}
