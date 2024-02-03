package dev.isxander.controlify.hid;

import com.sun.jna.Memory;
import io.github.libsdl4j.api.hidapi.SDL_hid_device;
import io.github.libsdl4j.api.hidapi.SdlHidApi;
import io.github.libsdl4j.jna.size_t;

public sealed interface HIDDevice permits HIDDevice.Hid4Java, HIDDevice.IDOnly, HIDDevice.SDLHidApi {
    int vendorID();

    int productID();

    String path();

    boolean supportsCommunication();
    void open();
    void close();
    int read(byte[] buffer);
    int write(byte[] buffer, int packetLength, byte reportId);

    final class Hid4Java implements HIDDevice {
        private final org.hid4java.HidDevice hidDevice;

        public Hid4Java(org.hid4java.HidDevice hidDevice) {
            this.hidDevice = hidDevice;
        }

        @Override
        public int vendorID() {
            return hidDevice.getVendorId();
        }

        @Override
        public int productID() {
            return hidDevice.getProductId();
        }

        @Override
        public String path() {
            return hidDevice.getPath();
        }

        @Override
        public boolean supportsCommunication() {
            return true;
        }

        @Override
        public void open() {
            hidDevice.open();
            hidDevice.setNonBlocking(true);
        }

        @Override
        public void close() {
            hidDevice.close();
        }

        @Override
        public int read(byte[] buffer) {
            return hidDevice.read(buffer);
        }

        @Override
        public int write(byte[] buffer, int packetLength, byte reportId) {
            return hidDevice.write(buffer, packetLength, reportId);
        }
    }

    record IDOnly(int vendorID, int productID, String path) implements HIDDevice {
        @Override
        public boolean supportsCommunication() {
            return false;
        }

        @Override
        public void open() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read(byte[] buffer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int write(byte[] buffer, int packetLength, byte reportId) {
            throw new UnsupportedOperationException();
        }
    }

    final class SDLHidApi implements HIDDevice {
        private final int vendorId;
        private final int productId;
        private final String path;

        private SDL_hid_device device;

        public SDLHidApi(int vendorId, int productId, String path) {
            this.vendorId = vendorId;
            this.productId = productId;
            this.path = path;
            this.device = null;
        }

        @Override
        public int vendorID() {
            return vendorId;
        }

        @Override
        public int productID() {
            return productId;
        }

        @Override
        public String path() {
            return path;
        }

        @Override
        public boolean supportsCommunication() {
            return true;
        }

        @Override
        public void open() {
            device = SdlHidApi.SDL_hid_open_path(path);
            SdlHidApi.SDL_hid_set_nonblocking(device, 1);
        }

        @Override
        public void close() {
            SdlHidApi.SDL_hid_close(device);
            device = null;
        }

        @Override
        public int read(byte[] buffer) {
            try (Memory memory = new Memory(buffer.length)) {
                int ret = SdlHidApi.SDL_hid_read(device, memory, new size_t(buffer.length));
                memory.read(0, buffer, 0, buffer.length);
                return ret;
            }
        }

        @Override
        public int write(byte[] buffer, int packetLength, byte reportId) {
            try (Memory memory = new Memory(buffer.length + 1)) {
                memory.setByte(buffer.length, reportId);
                memory.write(1, buffer, 0, buffer.length);
                return SdlHidApi.SDL_hid_write(device, memory, new size_t(buffer.length));
            }
        }
    }
}
