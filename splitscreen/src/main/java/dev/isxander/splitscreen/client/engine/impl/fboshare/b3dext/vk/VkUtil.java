package dev.isxander.splitscreen.client.engine.impl.fboshare.b3dext.vk;

import java.util.function.Supplier;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class VkUtil {
    public static void safeVkCall(Supplier<Integer> call, Supplier<String> errorSupplier) {
        int err = call.get();
        if (err != VK_SUCCESS) {
            throw new RuntimeException("Vulkan call failed (" + errorSupplier.get() + "): " + err);
        }
    }
}
