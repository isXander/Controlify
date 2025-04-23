package dev.isxander.controlify.splitscreen.mixins.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.splitscreen.SplitscreenBootstrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VirtualScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    /**
     * Hooks in to create a parent window for the splitscreen controller.
     * @param instance receiver type
     * @param screenSize arg1
     * @param videoModeName arg2
     * @param title arg3
     * @param original operation
     * @return client window
     */
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/VirtualScreen;newWindow(Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/blaze3d/platform/Window;"))
    private Window createParentWindow(VirtualScreen instance, DisplayData screenSize, String videoModeName, String title, Operation<Window> original) {
        SplitscreenBootstrapper.getController().ifPresent(controller ->
                controller.setupParentWindow(screenSize, ((VirtualScreenAccessor) (Object) instance).getScreenManager(), title));

        // create original client window
        return original.call(instance, screenSize, videoModeName, title);
    }
}
