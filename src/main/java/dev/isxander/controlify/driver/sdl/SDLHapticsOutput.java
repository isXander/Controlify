package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Pointer;
import dev.isxander.controlify.haptics.hd.HDHapticsBus;
import dev.isxander.sdl3java.api.audio.SDL_AudioDeviceID;
import dev.isxander.sdl3java.api.audio.SDL_AudioFormat;
import dev.isxander.sdl3java.api.audio.SDL_AudioSpec;
import dev.isxander.sdl3java.api.audio.SDL_AudioStream;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static dev.isxander.sdl3java.api.audio.SdlAudio.*;
import static dev.isxander.sdl3java.api.audio.SdlAudioConsts.*;
import static dev.isxander.sdl3java.api.error.SdlError.*;

public final class SDLHapticsOutput implements AutoCloseable {
    public static final int[] DUALSENSE_CHANNEL_MAP = new int[]{ -1, -1, 0, 1 };

    private final SDL_AudioDeviceID deviceId;
    private final SDL_AudioSpec deviceSpec;

    private final SDL_AudioStream stream;

    private final HDHapticsBus bus;

    private final int chunkFrames;
    private final int targetQueuedMs;

    private final ByteBuffer uploadByteBuffer;
    private final FloatBuffer uploadFloatBuffer;

    public SDLHapticsOutput(
            SDL_AudioDeviceID deviceId,
            SDL_AudioSpec deviceSpec,
            HDHapticsBus bus,
            int targetQueuedMs,
            int[] channelMap
    ) {
        this.deviceId = deviceId;
        this.deviceSpec = deviceSpec;
        this.bus = bus;
        this.chunkFrames = this.bus.chunkFrames();
        this.targetQueuedMs = targetQueuedMs;

        SDL_AudioSpec inputSpec = new SDL_AudioSpec();
        inputSpec.freq = bus.format().sampleRate();
        inputSpec.channels = bus.format().channels();
        inputSpec.format = new SDL_AudioFormat(SDL_AUDIO_F32LE);

        // create our stream
        this.stream = SDL_CreateAudioStream(inputSpec, this.deviceSpec);
        if (stream == null) {
            throw new IllegalStateException("SDL_CreateAudioStream failed: " + SDL_GetError());
        }

        // bind our created stream to the device
        if (!SDL_BindAudioStream(this.deviceId, stream)) {
            SDL_DestroyAudioStream(stream);
            throw new IllegalStateException("SDL_BindAudioStream failed: " + SDL_GetError());
        }

        // bind the stream to the device's correct channels
        if (!SDL_SetAudioStreamOutputChannelMap(stream, channelMap)) {
            System.out.println("SDL_SetAudioStreamOutputChannelMap failed: " + SDL_GetError());
        }

        int uploadBytes = this.chunkFrames * bus.format().bytesPerFrame();
        this.uploadByteBuffer = MemoryUtil.memAlloc(uploadBytes).order(ByteOrder.LITTLE_ENDIAN);
        this.uploadFloatBuffer = uploadByteBuffer.asFloatBuffer();
    }

    public HDHapticsBus bus() {
        return this.bus;
    }

    /**
     * pumps frames from the bus into the audio stream
     */
    public void pump() {
        int bytesPerFrame = bus.format().bytesPerFrame();

        int targetQueuedBytes = (int) ((bus.format().sampleRate() * (long) targetQueuedMs * bytesPerFrame) / 1000L);

        int queuedBytes = SDL_GetAudioStreamQueued(stream);
        if (queuedBytes < 0) {
            return;
        }

        while (queuedBytes < targetQueuedBytes) {
            int framesToQueue = chunkFrames;
            int samplesToQueue = framesToQueue * bus.format().channels();

            bus.ensureBufferedFrames(framesToQueue, chunkFrames);

            uploadFloatBuffer.clear();
            uploadFloatBuffer.limit(samplesToQueue);

            int samplesRead = bus.ringBuffer().readInto(uploadFloatBuffer, samplesToQueue);

            for (int i = samplesRead; i < samplesToQueue; i++) {
                uploadFloatBuffer.put(i, 0.0f);
            }

            long address = MemoryUtil.memAddress(uploadByteBuffer);
            Pointer dataPointer = new Pointer(address);

            int bytesToPut = framesToQueue * bytesPerFrame;
            boolean ok = SDL_PutAudioStreamData(stream, dataPointer, bytesToPut);
            if (!ok) {
                break;
            }

            queuedBytes += bytesToPut;
        }
    }

    @Override
    public void close() {
        SDL_DestroyAudioStream(stream);
        MemoryUtil.memFree(uploadByteBuffer);
    }
}

