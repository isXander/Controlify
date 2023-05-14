package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.PauseScreenProcessor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PauseScreen.class)
public class PauseScreenMixin implements ScreenProcessorProvider {
    @Shadow private @Nullable Button disconnectButton;

    @Unique private final PauseScreenProcessor processor
            = new PauseScreenProcessor((PauseScreen) (Object) this, () -> disconnectButton);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }
}
