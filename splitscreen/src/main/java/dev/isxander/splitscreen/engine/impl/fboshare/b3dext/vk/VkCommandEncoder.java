package dev.isxander.splitscreen.engine.impl.fboshare.b3dext.vk;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.isxander.splitscreen.engine.impl.fboshare.b3dext.SplitscreenCommandEncoder;
import dev.isxander.splitscreen.engine.impl.fboshare.b3dext.ShareHandle;
import dev.isxander.splitscreen.engine.impl.fboshare.b3dext.TimelineSemaphore;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.*;

import static dev.isxander.splitscreen.engine.impl.fboshare.b3dext.vk.VkUtil.safeVkCall;
import static org.lwjgl.vulkan.KHRExternalMemoryFd.*;
import static org.lwjgl.vulkan.KHRExternalMemoryWin32.*;
import static org.lwjgl.vulkan.KHRExternalSemaphoreFd.*;
import static org.lwjgl.vulkan.KHRExternalSemaphoreWin32.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.*;
import static org.lwjgl.vulkan.VK12.*;

// TODO: this does work on macOS as well, but with specialised metal apis.
// semaphores are also different, look up MTLSharedEvent
// 1. request IOSurface export when creating the image by chaining in VkExportMetalObjectsInfoEXT to VkImageCreateInfo
// 2. pull out the native IOSurfaceRef from VkImage by calling vkGetImageMetalObjectsEXT, second long in buffer is IOSurfaceRef
// 3. turn IOSurfaceRef into a sharable id by calling IOSurfaceGetID from objc
// 4. in the other process, look up the IOSurface by ID
// 5. import to vulkan by chaining in VkImportMetalIOSurfaceInfoEXT into VkMemoryAllocateInfo
public class VkCommandEncoder implements SplitscreenCommandEncoder {
    private final VkDevice vkDevice;

    public VkCommandEncoder(VkDevice vkDevice) {
        // requires following extensions
        // - VK_KHR_external_memory_fd (on linux)
        // - VK_KHR_external_semaphore_fd (on linux)
        // - VK_KHR_external_memory_win32 (on windows)
        // - VK_KHR_external_semaphore_win32 (on windows)
        // does not support macOS at this time.
        this.vkDevice = vkDevice;
    }

    @Override
    public GpuTexture importTexture(ShareHandle handle, int width, int height, TextureFormat format) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var extImgInfo = VkExternalMemoryImageCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_EXTERNAL_MEMORY_IMAGE_CREATE_INFO)
                    .pNext(0)
                    .handleTypes(switch (handle) {
                        case ShareHandle.Fd ignored -> VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_FD_BIT;
                        case ShareHandle.Win32 ignored -> VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_WIN32_BIT;
                    });

            var imgInfo = VkImageCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .pNext(extImgInfo.address())
                    .imageType(VK_IMAGE_TYPE_2D)
                    .format(format(format))
                    .extent(e -> e.width(width).height(height).depth(1))
                    .mipLevels(1)
                    .arrayLayers(1)
                    .samples(VK_SAMPLE_COUNT_1_BIT)
                    .tiling(VK_IMAGE_TILING_LINEAR)
                    .usage(VK_IMAGE_USAGE_SAMPLED_BIT) // only sampling from the imported texture, never rendering to it
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);

            var pImage = new long[1];
            safeVkCall(
                    () -> vkCreateImage(this.vkDevice, imgInfo, null, pImage),
                    () -> "failed to create image"
            );
            long image = pImage[0];

            var memReqs = VkMemoryRequirements.calloc(stack);
            vkGetImageMemoryRequirements(this.vkDevice, image, memReqs);

            Struct<?> importInfo = switch (handle) {
                case ShareHandle.Fd(int fd) -> VkImportMemoryFdInfoKHR.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_IMPORT_MEMORY_FD_INFO_KHR)
                        .pNext(0)
                        .handleType(VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_FD_BIT)
                        .fd(fd);
                case ShareHandle.Win32(long winHandle) -> VkImportMemoryWin32HandleInfoKHR.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_IMPORT_MEMORY_WIN32_HANDLE_INFO_KHR)
                        .pNext(0)
                        .handleType(VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_WIN32_BIT)
                        .handle(winHandle);
            };
            var allocInfo = VkMemoryAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .pNext(importInfo.address())
                    .allocationSize(memReqs.size())
                    .memoryTypeIndex(findMemoryType(memReqs.memoryTypeBits(), 0));

            var pMemory = new long[1];
            safeVkCall(
                    () -> vkAllocateMemory(this.vkDevice, allocInfo, null, pMemory),
                    () -> "failed to allocate memory for image"
            );
            long memory = pMemory[0];

            safeVkCall(
                    () -> vkBindImageMemory(this.vkDevice, image, memory, 0),
                    () -> "failed to bind shared memory to image"
            );

            // TODO
            //return new VkTexture(image, memory);
            return null;
        }
    }

    @Override
    public ShareHandle shareTexture(GpuTexture texture) {
        // TODO, just like importTexture, when allocating the memory of the image, you MUST chain in a VkExportMemoryAllocateInfo, this means it must be a specially allocated GpuTexture, not any old gpu texture
        long image = 0;
        long memory = 0;

        switch (Platform.get()) {
            case LINUX -> {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    var getFdInfo = VkMemoryGetFdInfoKHR.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_MEMORY_GET_FD_INFO_KHR)
                            .pNext(0)
                            .memory(memory)
                            .handleType(VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_FD_BIT);

                    var pFd = new int[1];
                    safeVkCall(
                            () -> vkGetMemoryFdKHR(this.vkDevice, getFdInfo, pFd),
                            () -> "failed to get file descriptor for memory"
                    );

                    return new ShareHandle.Fd(pFd[0]);
                }
            }
            case WINDOWS -> {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    var getFdInfo = VkMemoryGetWin32HandleInfoKHR.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_MEMORY_GET_WIN32_HANDLE_INFO_KHR)
                            .pNext(0)
                            .memory(memory)
                            .handleType(VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_WIN32_BIT);

                    PointerBuffer pHandle = stack.mallocPointer(1);
                    safeVkCall(
                            () -> vkGetMemoryWin32HandleKHR(this.vkDevice, getFdInfo, pHandle),
                            () -> "failed to get file descriptor for memory"
                    );

                    return new ShareHandle.Win32(pHandle.get(0));
                }
            }
            default -> throw new UnsupportedOperationException();
        }
    }

    @Override
    public VkSemaphore createSemaphore(long initialValue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var typeInfo = VkSemaphoreTypeCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
                    .pNext(0)
                    .semaphoreType(VK_SEMAPHORE_TYPE_TIMELINE)
                    .initialValue(initialValue);
            var semInfo = VkSemaphoreCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
                    .pNext(typeInfo.address())
                    .flags(0);

            var semaphore = new long[1];

            safeVkCall(
                    () -> vkCreateSemaphore(this.vkDevice, semInfo, null, semaphore),
                    () -> "failed to create timeline semaphore"
            );

            return new VkSemaphore(this.vkDevice, semaphore[0]);
        }
    }

    @Override
    public VkSemaphore importSemaphore(ShareHandle handle) {
        VkSemaphore semaphore = createSemaphore(0);

        return switch (handle) {
            case ShareHandle.Fd(int fd) -> {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    var importInfo = VkImportSemaphoreFdInfoKHR.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_IMPORT_SEMAPHORE_FD_INFO_KHR)
                            .semaphore(semaphore.getHandle())
                            .flags(0) // or VK_SEMAPHORE_IMPORT_TEMPORARY_BIT
                            .handleType(VK_EXTERNAL_SEMAPHORE_HANDLE_TYPE_OPAQUE_FD_BIT)
                            .fd(fd);

                    safeVkCall(
                            () -> vkImportSemaphoreFdKHR(this.vkDevice, importInfo),
                            () -> "failed to import semaphore from file descriptor"
                    );

                    yield semaphore;
                }
            }
            case ShareHandle.Win32(long winHandle) -> {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    var importInfo = VkImportSemaphoreWin32HandleInfoKHR.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_IMPORT_SEMAPHORE_WIN32_HANDLE_INFO_KHR)
                            .semaphore(semaphore.getHandle())
                            .flags(0) // or VK_SEMAPHORE_IMPORT_TEMPORARY_BIT
                            .handleType(VK_EXTERNAL_SEMAPHORE_HANDLE_TYPE_OPAQUE_WIN32_BIT)
                            .handle(winHandle);

                    safeVkCall(
                            () -> vkImportSemaphoreWin32HandleKHR(this.vkDevice, importInfo),
                            () -> "failed to import semaphore from Win32 handle"
                    );

                    yield semaphore;
                }
            }
        };
    }

    @Override
    public ShareHandle shareSemaphore(TimelineSemaphore semaphore) {
        VkSemaphore vkSemaphore = (VkSemaphore) semaphore;
        long semaphoreHandle = vkSemaphore.getHandle();

        return switch (Platform.get()) {
            case LINUX -> {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    var exportInfo = VkSemaphoreGetFdInfoKHR.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_SEMAPHORE_GET_FD_INFO_KHR)
                            .semaphore(semaphoreHandle)
                            .handleType(VK_EXTERNAL_SEMAPHORE_HANDLE_TYPE_OPAQUE_FD_BIT);

                    int[] fd = new int[1];
                    safeVkCall(
                            () -> vkGetSemaphoreFdKHR(this.vkDevice, exportInfo, fd),
                            () -> "failed to export semaphore to file descriptor"
                    );

                    yield new ShareHandle.Fd(fd[0]);
                }
            }
            case WINDOWS -> {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    var exportInfo = VkSemaphoreGetWin32HandleInfoKHR.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_SEMAPHORE_GET_WIN32_HANDLE_INFO_KHR)
                            .semaphore(semaphoreHandle)
                            .handleType(VK_EXTERNAL_SEMAPHORE_HANDLE_TYPE_OPAQUE_WIN32_BIT);

                    PointerBuffer handleBuffer = stack.mallocPointer(1);
                    safeVkCall(
                            () -> vkGetSemaphoreWin32HandleKHR(this.vkDevice, exportInfo, handleBuffer),
                            () -> "failed to export semaphore to Win32 handle"
                    );

                    yield new ShareHandle.Win32(handleBuffer.get(0));
                }
            }
            default -> throw new UnsupportedOperationException("Unsupported platform: " + Platform.get());
        };
    }

    // You need to implement this to pick a suitable memoryTypeIndex:
    private static int findMemoryType(int typeFilter, int properties) {
        // … query vkGetPhysicalDeviceMemoryProperties and scan for (typeFilter & (1<<i))
        return 0;
    }

    // if mc implemented vulkan, this would already be done, in VkConst.toVkFormat
    private static int format(TextureFormat format) {
        return switch (format) {
            case RGBA8 -> VK_FORMAT_R8G8B8A8_UNORM;
            case RED8 -> VK_FORMAT_R8_UNORM;
            case DEPTH32 -> VK_FORMAT_D32_SFLOAT;
        };
    }
}
