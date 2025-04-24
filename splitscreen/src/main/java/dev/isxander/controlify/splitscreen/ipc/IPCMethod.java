package dev.isxander.controlify.splitscreen.ipc;

/**
 * Represents the method of connection/hosting of the IPC.
 */
public sealed interface IPCMethod {
    /**
     * Binds loopback to the local machine over the desired port.
     * @param port port to bind to. Default is <code>54321</code>.
     */
    record TCP(int port) implements IPCMethod {
        public static final TCP DEFAULT = new TCP(54321);
    }

    /**
     * Uses AF_UNIX sockets (file descriptor on Unix or named pipe on Windows)
     * to communicate between clients.
     * @param path path to socket
     */
    record Unix(String path) implements IPCMethod {
        public static final Unix LOCAL_UNIX = new Unix("/tmp/minecraft-controlify-splitscreen.sock");
        public static final Unix LOCAL_WINDOWS = new Unix("\\\\.\\pipe\\minecraft-controlify-splitscreen");
    }
}
