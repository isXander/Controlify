package dev.isxander.controlify.mixins.compat.screen.yacl;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.compatibility.screen.ScreenProcessorProvider;
import dev.isxander.controlify.compatibility.yacl.YACLScreenProcessor;
import dev.isxander.yacl.gui.YACLScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(YACLScreen.class)
public class YACLScreenMixin implements ScreenProcessorProvider {
    @Unique private final YACLScreenProcessor controlify$processor = new YACLScreenProcessor((YACLScreen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return controlify$processor;
    }
}
