package dev.isxander.splitscreen.engine.impl.fboshare.consumer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.isxander.splitscreen.engine.impl.fboshare.b3dext.SharedRenderTarget;
import dev.isxander.splitscreen.engine.impl.fboshare.b3dext.SharedTexture;
import dev.isxander.splitscreen.engine.impl.fboshare.b3dext.SplitscreenCommandEncoder;
import dev.isxander.splitscreen.engine.impl.fboshare.b3dext.TimelineSemaphore;

public final class SharedWindowMemory {
    private int currentFrame;
    private final RenderTarget[] frames;
    private final TimelineSemaphore semaphore;

    public SharedWindowMemory(
            int currentFrame,
            RenderTarget[] frames,
            TimelineSemaphore semaphore
    ) {
        this.currentFrame = currentFrame;
        this.frames = frames;
        this.semaphore = semaphore;
    }

    public RenderTarget waitForNextFrame() {
        // increment the current frame
        this.currentFrame = (this.currentFrame() + 1) % this.ringSize();

        // get the current frame
        RenderTarget frame = this.frames[this.currentFrame];

        // wait for the current frame to be signaled done
        this.semaphore().acquire(this.currentFrame, new GpuBuffer[0], new GpuTexture[]{ frame.getColorTexture(), frame.getDepthTexture() }, 100);

        // give the current frame to the caller
        return frame;
    }

    public int currentFrame() {
        return currentFrame;
    }

    public RenderTarget[] frames() {
        return frames;
    }

    public int ringSize() {
        return frames.length;
    }

    public TimelineSemaphore semaphore() {
        return semaphore;
    }

    public static RenderTarget importRenderTarget(SharedRenderTarget sharedRenderTarget) {
        SplitscreenCommandEncoder encoder = SplitscreenCommandEncoder.UNSUPPORTED;

        SharedTexture sColourTex = sharedRenderTarget.colour();
        GpuTexture colourTex = encoder.importTexture(sColourTex.handle(), sharedRenderTarget.width(), sharedRenderTarget.height(), sColourTex.format());

        SharedTexture sDepthTex = sharedRenderTarget.depth();
        GpuTexture depthTex = encoder.importTexture(sDepthTex.handle(), sharedRenderTarget.width(), sharedRenderTarget.height(), sDepthTex.format());

        // TODO
        return null;
    }
}
