package dev.isxander.controlify.mixins.feature.screenop.yacl;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.yacl.YACLScreenProcessor;
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
