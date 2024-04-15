package dev.isxander.controlify.platform.network;

@FunctionalInterface
public interface PacketListener<T> {
    void listen(T packet);
}
