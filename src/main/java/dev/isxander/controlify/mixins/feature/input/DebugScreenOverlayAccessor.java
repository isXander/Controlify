package dev.isxander.controlify.mixins.feature.input;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DebugScreenOverlay.class)
public interface DebugScreenOverlayAccessor {
    @Accessor
    boolean isRenderDebug();
}
