package dev.isxander.controlify.splitscreen.util;

import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.SocketConnectionMethod;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class SocketUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final boolean IS_AF_UNIX_SUPPORTED;
    static {
        boolean isAfUnixSupported;
        // JDK 16+ : JEP 380 adds Unix‑domain sockets on supported platforms.
        try (ServerSocketChannel ignored = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            isAfUnixSupported = true; // if it opens, we’re good
        } catch (UnsupportedOperationException e) {
            isAfUnixSupported = false; // OS/JDK doesn’t support it
        } catch (IOException io) {
            // Socket exists / permission issue etc. – treat as supported,
            // because the protocol family was accepted.
            isAfUnixSupported = true;
        }

        IS_AF_UNIX_SUPPORTED = isAfUnixSupported;
    }

    public static boolean isSocketListening(SocketConnectionMethod method) {
        switch (method) {
            case SocketConnectionMethod.TCP(int port) -> {
                try (ServerSocketChannel server = ServerSocketChannel.open()) {
                    server.bind(new InetSocketAddress(port));
                    return false; // Socket is not open
                } catch (AlreadyBoundException | BindException e) {
                    return true;  // Socket is open
                } catch (IOException io) {
                    throw new RuntimeException(io);
                }
            }
            case SocketConnectionMethod.Unix(String path) -> {
                if (!isAfUnixSupported()) {
                    throw new UnsupportedOperationException("AF_UNIX sockets are not supported on this platform.");
                }

                var socketPath = Path.of(path);
                var address = UnixDomainSocketAddress.of(path);

                // Utilise try-with-resources to ensure the socket is closed automatically
                try (SocketChannel socket = SocketChannel.open(StandardProtocolFamily.UNIX)) {
                    LOGGER.info("Attempting to connect to {}", socketPath);
                    socket.connect(address);
                    // If connection succeeds, something is listening.
                    LOGGER.info("Connection successful to {}. Socket is active.", socketPath);
                    // The try-with-resources will close the socket upon exiting this block.
                    return true;
                } catch (ConnectException e) {
                    // Connection refused means the file might exist,
                    // but nothing is actively listening/accepting connections.
                    LOGGER.info("Connection refused for {}: {}", socketPath, e.getMessage());
                    try {
                        Files.deleteIfExists(socketPath);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    return false;
                } catch (NoSuchFileException e) {
                    // The socket file doesn't even exist.
                    LOGGER.info("Socket file not found: {}: {}", socketPath, e.getMessage());
                    return false;
                } catch (IOException e) {
                    // Handle other potential I/O errors (e.g., permission denied)
                    LOGGER.info("IOException while checking socket {}", socketPath, e);
                    return false;
                }
            }
        }
    }

    /**
     * Checks if the OS supports AF_UNIX sockets.
     * @return true if AF_UNIX sockets are supported, false otherwise.
     */
    public static boolean isAfUnixSupported() {
        return IS_AF_UNIX_SUPPORTED;
    }
}
