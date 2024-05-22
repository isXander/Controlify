package dev.isxander.controlify.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.function.Supplier;

public class CUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger("Controlify");

    public static final Version VERSION = FabricLoader.getInstance().getModContainer("controlify")
            .orElseThrow().getMetadata().getVersion();
    
    public static ResourceLocation rl(String path) {
        return new ResourceLocation("controlify", path);
    }

    /**
     * Opens a URI using the system's default handler.
     * Required because Minecraft's implementation converts all URIs to URLs,
     * which prevents the use of custom protocols like steam://
     * @param uri the URI to open
     */
    public static void openUri(String uri) {
        try {
            String[] command = URIOpener.get().openArguments(URI.create(uri));

            Process process = Runtime.getRuntime().exec(command);
            process.getInputStream().close();
            process.getOutputStream().close();
            process.getErrorStream().close();
        } catch (IOException | SecurityException e) {
            LOGGER.error("Failed to open URI: {}", uri, e);
        }
    }

    public static <T> Supplier<T> lazyInit(Supplier<T> supplier) {
        return new Supplier<>() {
            private T created = null;

            @Override
            public T get() {
                if (created == null)
                    created = supplier.get();
                return created;
            }
        };
    }

    private enum URIOpener {
        WINDOWS(Util.OS.WINDOWS),
        OSX(Util.OS.OSX),
        LINUX(Util.OS.LINUX),
        SOLARIS(Util.OS.SOLARIS);

        private final Util.OS mcOS;

        URIOpener(Util.OS mcOS) {
            this.mcOS = mcOS;
        }

        public String[] openArguments(URI uri) {
            return switch (this.mcOS) {
                case WINDOWS -> new String[]{"rundll32", "url.dll,FileProtocolHandler", uri.toString()};
                case OSX -> new String[]{"open", uri.toString()};
                case LINUX, SOLARIS -> new String[]{"xdg-open", uri.toString()};
                default -> throw new UnsupportedOperationException("Unsupported OS: " + this.mcOS);
            };
        }

        public static URIOpener get() {
            return switch (Util.getPlatform()) {
                case WINDOWS -> WINDOWS;
                case OSX -> OSX;
                case LINUX -> LINUX;
                case SOLARIS -> SOLARIS;
                default -> throw new UnsupportedOperationException("Unsupported OS: " + Util.getPlatform());
            };
        }
    }
}
