package dev.isxander.controlify.driver.sdl;

import dev.isxander.sdl3java.api.guid.SDL_GUID;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public record DecodedGUID(
        short bus,
        short crc,
        short vendor,
        @Nullable Short product,
        @Nullable Short version,
        @Nullable Byte driverSignature,
        @Nullable Byte driverData,
        @Nullable String productName
) {
    private static final Map<Byte, String> driverSigToName = Map.of(
            ((byte) 'x'), "windows/XInput",
            ((byte) 'w'), "windows/Windows Gaming Input",
            ((byte) 'r'), "windows/Raw Input",
            ((byte) 'g'), "gdk/GameInput",
            ((byte) 'h'), "HIDAPI",
            ((byte) 'm'), "apple/MFI",
            ((byte) 'v'), "Virtual"

    );

    public static DecodedGUID fromGUID(SDL_GUID guid) {
        return fromBytes(guid.data);
    }

    public static DecodedGUID fromString(String guid) {
        guid = guid.replace("-", "");
        return fromBytes(guid.getBytes(StandardCharsets.UTF_8));
    }

    public static DecodedGUID fromBytes(byte[] guid) {
        Validate.isTrue(guid.length == 16, "GUID must be 16 bytes long");

        short bus = readShortLE(guid, 0);
        short crc = readShortLE(guid, 2);
        short vendor = readShortLE(guid, 4);
        Short product = null;
        Short version = null;
        Byte driverSignature = null;
        Byte driverData = null;
        String productName = null;

        if (vendor != 0) {
            // Case A: vendor != 0
            // Layout:
            //   [4..5]   vendor (LE)
            //   [6..7]   0 (padding)
            //   [8..9]   product (LE)
            //   [10..11] 0 (padding)
            //   [12..13] version (LE)
            //   [14]     driver_signature
            //   [15]     driver_data
            product         = readShortLE(guid, 8);
            version         = readShortLE(guid, 12);
            driverSignature = guid[14];
            driverData      = guid[15];
        } else {
            // Case B: vendor == 0
            //   => Possibly a truncated product_name in [4..13 or 15].
            //   => If driverSignature != 0, then [14..15] = signature/data
            //   => If driverSignature == 0, then all [4..15] could be product_name.

            byte driverSignatureRaw = guid[14];
            byte driverDataRaw = guid[15];

            if (driverSignatureRaw != 0) {
                // We assume [14..15] is driver_signature/driver_data,
                // so product_name is in [4..13] (up to 10 bytes + null terminator).
                driverSignature = driverSignatureRaw;
                driverData      = driverDataRaw;
                productName     = readCString(guid, 4, 10);
            } else {
                // If driver_signature == 0, likely no signature stored.
                // Then [4..15] can be product_name (up to 12 bytes + null terminator).
                productName = readCString(guid, 4, 12);
            }
        }

        return new DecodedGUID(bus, crc, vendor, product, version, driverSignature, driverData, productName);
    }

    public static String getDriverHint(byte driverSignature) {
        return driverSigToName.getOrDefault(driverSignature, "Unrecognised: '" + (char) driverSignature + "'");
    }

    public String getDriverHint() {
        return driverSignature == null ? "None" : getDriverHint(driverSignature);
    }

    private static short readShortLE(byte[] data, int offset) {
        int low = data[offset] & 0xFF;
        int high = (data[offset + 1] & 0xFF) << 8;
        return (short) (low | high);
    }

    private static String readCString(byte[] data, int offset, int length) {
        int end = offset;
        int max = Math.min(offset + length, data.length);
        while (end < max && data[end] != 0) {
            end++;
        }

        return new String(data, offset, end - offset, StandardCharsets.UTF_8);
    }
}
