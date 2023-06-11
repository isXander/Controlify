package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.utils.Log;
import org.hid4java.HidDevice;

import java.util.Arrays;

public class SteamDeckDriver implements GyroDriver, BasicGamepadInputDriver {
    private static final int cInputRecordLen = 8; // Number of bytes that are read from the hid device per 1 byte of HID
    private static final int cByteposInput = 4; // Position in the raw hid data where HID data byte is
    private static final byte[] startMarker = new byte[] { 0x01, 0x00, 0x09, 0x40 }; // Beginning of every Steam deck HID frame

    private final HidDevice hidDevice;
    private int interval = 0;

    private GamepadState.GyroState gyroDelta = GamepadState.GyroState.ORIGIN;
    private BasicGamepadState basicGamepadState = new BasicGamepadState(GamepadState.AxesState.EMPTY, GamepadState.ButtonState.EMPTY);

    public SteamDeckDriver(HidDevice hidDevice) {
        this.hidDevice = hidDevice;
        this.hidDevice.open();
        this.hidDevice.setNonBlocking(true);
    }

    @Override
    public void update() {
        if (interval == 0)
            keepAlive();
        interval = (interval + 1) % 120;

        byte[] data = new byte[64];
        int readCnt = hidDevice.read(data);

        if (readCnt == 0) {
            Log.LOGGER.warn("No data available.");
        }
        if (readCnt == -1) {
            Log.LOGGER.warn("Error reading data.");
        }

        System.out.println(Arrays.toString(data));

        if (!checkData(data, readCnt)) return;

        Frame frame = Frame.fromBytes(data);
        System.out.println(frame);
        readFrame(frame);
    }

    private void keepAlive() {
        hidDevice.sendFeatureReport(new byte[0], (byte) 8);
    }

    private void readFrame(Frame frame) {
        gyroDelta = new GamepadState.GyroState(
                frame.gyroAxisFrontToBack,
                frame.gyroAxisTopToBottom,
                frame.gyroAxisRightToLeft
        );

        basicGamepadState = new BasicGamepadState(
                new GamepadState.AxesState(
                        frame.leftStickX,
                        frame.leftStickY,
                        frame.rightStickX,
                        frame.rightStickY,
                        frame.l2Analog,
                        frame.r2Analog
                ),
                new GamepadState.ButtonState(
                        ((frame.buttons1BitMap >> 7) & 1) == 1,
                        ((frame.buttons1BitMap >> 5) & 1) == 1,
                        ((frame.buttons1BitMap >> 6) & 1) == 1,
                        ((frame.buttons1BitMap >> 4) & 1) == 1,
                        ((frame.buttons1BitMap >> 3) & 1) == 1,
                        ((frame.buttons1BitMap >> 2) & 1) == 1,
                        ((frame.buttons1BitMap >> 12) & 1) == 1,
                        ((frame.buttons1BitMap >> 14) & 1) == 1,
                        ((frame.buttons1BitMap >> 13) & 1) == 1,
                        false, false, false, false,
                        ((frame.buttons1BitMap >> 1) & 1) == 1,
                        ((frame.buttons1BitMap >> 0) & 1) == 1
                )
        );
    }

    private boolean checkData(byte[] data, int readCnt) {
        int first4Bytes = 0xFFFF0002;
        int first4BytesAlt = 0xFFFF0001;

        boolean inputFail = readCnt < data.length;
        boolean startMarkerFail = false;

        if (!inputFail) {
            startMarkerFail = first4Bytes(data) != first4Bytes;
            if (startMarkerFail && first4Bytes(data) == first4BytesAlt) {
                startMarkerFail = false;

                for (int i = cByteposInput, j = 0; j < startMarker.length; j++, i += cInputRecordLen) {
                    if (data[i] != startMarker[j]) {
                        startMarkerFail = true;
                        break;
                    }
                }
            }
        }

        return !inputFail && !startMarkerFail;
    }

    private int first4Bytes(byte[] data) {
        return (data[0] << 24) | (data[1] << 16) | (data[2] << 8) | data[3];
    }

    @Override
    public GamepadState.GyroState getGyroState() {
        return gyroDelta;
    }

    @Override
    public BasicGamepadState getBasicGamepadState() {
        return basicGamepadState;
    }

    @Override
    public boolean isGyroSupported() {
        return true;
    }

    @Override
    public void close() {
        hidDevice.close();
    }

    @Override
    public String getBasicGamepadDetails() {
        return "SteamDeck HIDAPI";
    }

    @Override
    public String getGyroDetails() {
        return "SteamDeck HIDAPI";
    }

    // https://github.com/kmicki/SteamDeckGyroDSU/blob/574745406011cc2433fc6f179446ecc836180aa4/inc/sdgyrodsu/sdhidframe.h
    private record Frame(
            int header,
            int increment,

            // Buttons 1:
            //      .0 - R2 full pull
            //      .1 - L2 full pull
            //      .2 - R1
            //      .3 - L1
            //      .4 - Y
            //      .5 - B
            //      .6 - X
            //      .7 - A
            //      .12 - Select
            // .13 - STEAM
            // .14 - Start
            // .15 - L5
            //      .16 - R5
            //      .17 - L trackpad click
            //      .18 - R trackpad click
            //      .19 - L trackpad touch
            //      .20 - R trackpad touch
            //      .22 - L3
            //      .26 - R3
            int buttons1BitMap,

            // Buttons 2:
            //  .9 - L4
            //  .10 - R4
            //  .14 - L3 touch
            //  .15 - R3 touch
            //       .18 - (...)
            int buttons2BitMap,

            short leftTrackpadX,
            short leftTrackpadY,
            short rightTrackpadX,
            short rightTrackpadY,

            short accelAxisRightToLeft,
            short accelAxisTopToBottom,
            short accelAxisFrontToBack,

            short gyroAxisRightToLeft,
            short gyroAxisTopToBottom,
            short gyroAxisFrontToBack,

            short unknown1,
            short unknown2,
            short unknown3,
            short unknown4,

            short l2Analog,
            short r2Analog,
            short leftStickX,
            short leftStickY,
            short rightStickX,
            short rightStickY,

            short leftTrackpadPushForce,
            short rightTrackpadPushForce,
            short leftStickTouchCoverage,
            short rightStickTouchCoverage
    ) {
        // i love github copilot
        public static Frame fromBytes(byte[] bytes) {
            return new Frame(
                    (bytes[0] << 24) | (bytes[1] << 16) | (bytes[2] << 8) | bytes[3],
                    (bytes[4] << 24) | (bytes[5] << 16) | (bytes[6] << 8) | bytes[7],
                    (bytes[8] << 24) | (bytes[9] << 16) | (bytes[10] << 8) | bytes[11],
                    (bytes[12] << 24) | (bytes[13] << 16) | (bytes[14] << 8) | bytes[15],
                    (short) ((bytes[16] << 8) | bytes[17]),
                    (short) ((bytes[18] << 8) | bytes[19]),
                    (short) ((bytes[20] << 8) | bytes[21]),
                    (short) ((bytes[22] << 8) | bytes[23]),
                    (short) ((bytes[24] << 8) | bytes[25]),
                    (short) ((bytes[26] << 8) | bytes[27]),
                    (short) ((bytes[28] << 8) | bytes[29]),
                    (short) ((bytes[30] << 8) | bytes[31]),
                    (short) ((bytes[32] << 8) | bytes[33]),
                    (short) ((bytes[34] << 8) | bytes[35]),
                    (short) ((bytes[36] << 8) | bytes[37]),
                    (short) ((bytes[38] << 8) | bytes[39]),
                    (short) ((bytes[40] << 8) | bytes[41]),
                    (short) ((bytes[42] << 8) | bytes[43]),
                    (short) ((bytes[44] << 8) | bytes[45]),
                    (short) ((bytes[46] << 8) | bytes[47]),
                    (short) ((bytes[48] << 8) | bytes[49]),
                    (short) ((bytes[50] << 8) | bytes[51]),
                    (short) ((bytes[52] << 8) | bytes[53]),
                    (short) ((bytes[54] << 8) | bytes[55]),
                    (short) ((bytes[56] << 8) | bytes[57]),
                    (short) ((bytes[58] << 8) | bytes[59]),
                    (short) ((bytes[60] << 8) | bytes[61]),
                    (short) ((bytes[62] << 8) | bytes[63])
            );
        }
    }

    public static boolean isSteamDeck(HidDevice hid) {
        return hid.getVendorId() == 0x28DE && hid.getProductId() == 0x1205;
    }
}
