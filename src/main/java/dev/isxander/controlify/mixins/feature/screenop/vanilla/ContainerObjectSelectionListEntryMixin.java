package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.CustomFocus;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerObjectSelectionList.Entry.class)
public abstract class ContainerObjectSelectionListEntryMixin implements CustomFocus {
    @Shadow public abstract @Nullable GuiEventListener getFocused();

    @Override
    public GuiEventListener getCustomFocus() {
        return getFocused();
    }
}
