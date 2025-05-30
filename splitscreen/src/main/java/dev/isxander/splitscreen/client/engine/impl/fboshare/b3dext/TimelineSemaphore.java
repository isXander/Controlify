package dev.isxander.splitscreen.client.engine.impl.fboshare.b3dext;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.textures.GpuTexture;

public interface TimelineSemaphore {
    /**
     * Signals the semaphore, allowing the given textures to be used.
     *
     * @param buffers the buffers to create a fence for
     * @param textures the textures to create a fence for
     */
    void signal(long value, GpuBuffer[] buffers, GpuTexture[] textures);

    /**
     * Waits for the semaphore to be signaled, allowing the given textures to be used.
     *
     * @param buffers the buffers to wait for
     * @param textures the textures to wait for
     */
    void acquire(long value, GpuBuffer[] buffers, GpuTexture[] textures, long timeoutNs);


    void close();
}
