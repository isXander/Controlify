package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.WorldListEntryComponentProcessor;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WorldSelectionList.WorldListEntry.class)
public class WorldSelectionListEntryMixin implements ComponentProcessorProvider {
    @Unique
    private final WorldListEntryComponentProcessor controlify$processor = new WorldListEntryComponentProcessor();

    @Override
    public ComponentProcessor componentProcessor() {
        return controlify$processor;
    }
}
