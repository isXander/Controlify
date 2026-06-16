package dev.isxander.controlify.mixins.feature.steamdeck;

import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(Screenshot.class)
public interface ScreenshotAccessor {
    @Invoker("getFile")
    static File controlify$invokeGetFile(File file) {
        throw new AssertionError();
    }
}
