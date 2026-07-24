package dev.isxander.controlify.hid;

public record HIDDevice(HIDID hidid, String path) {
    public int vendorId() {
		return hidid().vendorId();
	}
    public int productId() {
		return hidid().productId();
	}
}
