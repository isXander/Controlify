package dev.isxander.controlify.splitscreen;

public sealed interface SocketConnectionMethod {
    record TCP(int port) implements SocketConnectionMethod {
        public static final TCP DEFAULT = new TCP(54321);
    }
    record Unix(String path) implements SocketConnectionMethod {
        public static final Unix LOCAL_UNIX = new Unix("/tmp/minecraft-controlify-splitscreen.sock");
        public static final Unix LOCAL_WINDOWS = new Unix("\\\\.\\pipe\\minecraft-controlify-splitscreen");
    }
}
