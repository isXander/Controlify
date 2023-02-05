package dev.isxander.controlify.mixins.compat.screen.vanilla;

import dev.isxander.controlify.compatibility.screen.component.ComponentProcessor;
import dev.isxander.controlify.compatibility.screen.component.ComponentProcessorProvider;
import dev.isxander.controlify.compatibility.vanilla.ServerSelectionListEntryComponentProcessor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerSelectionList.Entry.class)
public class ServerSelectionListEntryMixin implements ComponentProcessorProvider {
    @Unique private final ServerSelectionListEntryComponentProcessor controlify$componentProcessor
            = new ServerSelectionListEntryComponentProcessor();

    @Override
    public ComponentProcessor componentProcessor() {
        return ((ServerSelectionList.Entry) (Object) this) instanceof ServerSelectionList.LANHeader
                ? ComponentProcessor.EMPTY
                : controlify$componentProcessor;
    }
}
