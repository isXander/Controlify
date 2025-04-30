package dev.isxander.controlify.splitscreen.engine.impl.fboshare.b3dext.vk;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.isxander.controlify.splitscreen.engine.impl.fboshare.b3dext.TimelineSemaphore;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkSemaphoreSignalInfo;
import org.lwjgl.vulkan.VkSemaphoreWaitInfo;

import java.nio.LongBuffer;

import static dev.isxander.controlify.splitscreen.engine.impl.fboshare.b3dext.vk.VkUtil.safeVkCall;
import static org.lwjgl.vulkan.VK12.*;

public class VkSemaphore implements TimelineSemaphore {

    private final VkDevice device;
    private final long semaphore;

    public VkSemaphore(VkDevice device, long semaphore) {
        this.device = device;
        this.semaphore = semaphore;
    }

    @Override
    public void signal(long value, GpuBuffer[] buffers, GpuTexture[] textures) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var info = VkSemaphoreSignalInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SEMAPHORE_SIGNAL_INFO)
                    .pNext(0)
                    .semaphore(this.semaphore)
                    .value(value);

            safeVkCall(
                    () -> vkSignalSemaphore(this.device, info),
                    () -> "failed to signal semaphore"
            );
        }
    }

    @Override
    public void acquire(long value, GpuBuffer[] buffers, GpuTexture[] textures, long timeoutNs) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer semBuf = stack.longs(this.semaphore);
            LongBuffer valBuf = stack.longs(value);

            var info = VkSemaphoreWaitInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SEMAPHORE_WAIT_INFO)
                    .pNext(0)
                    .flags(0)
                    .pSemaphores(semBuf)
                    .pValues(valBuf);

            safeVkCall(
                    () -> vkWaitSemaphores(this.device, info, timeoutNs),
                    () -> "failed to wait for semaphore"
            );
        }
    }

    public long getHandle() {
        return this.semaphore;
    }

    @Override
    public void close() {
        vkDestroySemaphore(this.device, this.semaphore, null);
    }

}
