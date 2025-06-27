package dev.isxander.splitscreen.client.ipc;

import java.nio.file.Path;
import java.util.Optional;

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
        public static Optional<Unix> inDirectory(Path path) {
            String pathStr = path.resolve("controlify-splitscreen.sock").toAbsolutePath().toString();
            // TODO: current limitation with relaunch passing this as a jvm arg and this can't have spaces in right now
            if (pathStr.contains(" ")) return Optional.empty();
            return Optional.of(new Unix(pathStr));
        }
    }
}
