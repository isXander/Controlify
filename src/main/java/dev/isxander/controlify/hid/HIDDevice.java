package dev.isxander.controlify.hid;

public sealed interface HIDDevice permits HIDDevice.Hid4Java, HIDDevice.IDOnly {
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
}
