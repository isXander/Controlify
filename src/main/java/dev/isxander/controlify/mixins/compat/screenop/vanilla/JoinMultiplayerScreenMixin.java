package dev.isxander.controlify.mixins.compat.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.JoinMultiplayerScreenProcessor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin implements ScreenProcessorProvider {
    @Shadow protected ServerSelectionList serverSelectionList;

    @Unique private final JoinMultiplayerScreenProcessor controlify$processor
            = new JoinMultiplayerScreenProcessor((JoinMultiplayerScreen) (Object) this, serverSelectionList);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return controlify$processor;
    }
}
