package dev.isxander.splitscreen.client.engine.impl.fboshare.b3dext;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;

public interface SplitscreenCommandEncoder {
    SplitscreenCommandEncoder UNSUPPORTED = new SplitscreenCommandEncoder() {
        @Override
        public GpuTexture importTexture(ShareHandle handle, int width, int height, TextureFormat format) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ShareHandle shareTexture(GpuTexture texture) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TimelineSemaphore createSemaphore(long initialValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TimelineSemaphore importSemaphore(ShareHandle handle) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ShareHandle shareSemaphore(TimelineSemaphore semaphore) {
            throw new UnsupportedOperationException();
        }
    };

    GpuTexture importTexture(ShareHandle handle, int width, int height, TextureFormat format);

    ShareHandle shareTexture(GpuTexture texture);

    TimelineSemaphore createSemaphore(long initialValue);

    TimelineSemaphore importSemaphore(ShareHandle handle);

    ShareHandle shareSemaphore(TimelineSemaphore semaphore);
}
