package dev.isxander.splitscreen.mixins.core;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Window.class)
public interface WindowAccessor {
    /**
     * Gets GLFW to query the window's framebuffer size so MC renders correctly.
     */
    @Invoker
    void callRefreshFramebufferSize();
}
