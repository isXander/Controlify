package dev.isxander.controlify.splitscreen.util;

import dev.isxander.controlify.splitscreen.SocketConnectionMethod;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SocketUtil {
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

    public static boolean isSocketOpen(SocketConnectionMethod method) {
        switch (method) {
            case SocketConnectionMethod.TCP(int port) -> {
                try (ServerSocketChannel server = ServerSocketChannel.open()) {
                    server.bind(new InetSocketAddress(port));
                    return false; // Socket is not open
                } catch (AlreadyBoundException e) {
                    return true;  // Socket is open
                } catch (IOException io) {
                    // Handle other IO exceptions (e.g., permission issues)
                    return true;  // Socket is open
                }
            }
            case SocketConnectionMethod.Unix(String path) -> {
                if (!isAfUnixSupported()) {
                    throw new UnsupportedOperationException("AF_UNIX sockets are not supported on this platform.");
                }

                var socketPath = Path.of(path);
                var address = UnixDomainSocketAddress.of(path);

                // If a stale socket file is lying around (common after crashes), remove it first;
                // otherwise bind() will throw “Address already in use”.
                try { Files.deleteIfExists(socketPath); } catch (IOException ignored) {}

                try (ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
                    server.bind(address);
                    return false; // Socket is not open
                } catch (AlreadyBoundException e) {
                    return true;  // Socket is open
                } catch (IOException io) {
                    // Handle other IO exceptions (e.g., permission issues)
                    return true;  // Socket is open
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
