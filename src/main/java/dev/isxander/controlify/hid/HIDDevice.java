package dev.isxander.controlify.hid;

import com.sun.jna.Memory;
import dev.isxander.sdl3java.api.hidapi.SDL_hid_device;
import dev.isxander.sdl3java.api.hidapi.SdlHidApi;
import dev.isxander.sdl3java.jna.size_t;

public sealed interface HIDDevice {
    int vendorId();
    int productId();

    String path();

    boolean supportsCommunication();

    void open();
    void close();

    int read(byte[] buffer);
    int write(byte[] buffer, int packetLength, byte reportId);

    default HIDIdentifier asIdentifier() {
        return new HIDIdentifier(this.vendorId(), this.productId());
    }

    final class Hid4Java implements HIDDevice {
        private final org.hid4java.HidDevice hidDevice;

        public Hid4Java(org.hid4java.HidDevice hidDevice) {
            this.hidDevice = hidDevice;
        }

        @Override
        public int vendorId() {
            return hidDevice.getVendorId();
        }

        @Override
        public int productId() {
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

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HIDDevice hid) {
                return this.asIdentifier().equals(hid.asIdentifier());
            }
            return false;
        }
    }

    record IDOnly(int vendorId, int productId, String path) implements HIDDevice {
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

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HIDDevice hid) {
                return this.asIdentifier().equals(hid.asIdentifier());
            }
            return false;
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
        public int vendorId() {
            return vendorId;
        }

        @Override
        public int productId() {
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

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HIDDevice hid) {
                return this.asIdentifier().equals(hid.asIdentifier());
            }
            return false;
        }
    }
}
