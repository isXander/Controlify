package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.driver.sdl.DualsenseOggAudioStream;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HapticBufferLibrary {
    private final Map<ResourceLocation, CompletableFuture<HapticBuffer>> bufferMap;
    private final ResourceProvider resources;

    public static final HapticBufferLibrary INSTANCE = new HapticBufferLibrary(Minecraft.getInstance().getResourceManager());

    private HapticBufferLibrary(ResourceProvider resources) {
        this.bufferMap = new HashMap<>();
        this.resources = resources;
    }

    public CompletableFuture<HapticBuffer> getHaptic(ResourceLocation haptic) {
        return bufferMap.computeIfAbsent(haptic, this::createHapticBuffer);
    }

    private CompletableFuture<HapticBuffer> createHapticBuffer(ResourceLocation haptic) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream is = resources.open(haptic)) {
                try (DualsenseOggAudioStream stream = new DualsenseOggAudioStream(is)) {
                    ByteBuffer audioBuf = stream.readAll();
                    byte[] audio = new byte[audioBuf.capacity()];
                    audioBuf.get(audio);

                    return new HapticBuffer(audio, stream.getFormat());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, Util.backgroundExecutor());
    }

    public record HapticBuffer(byte[] audio, AudioFormat format) {
    }

}
