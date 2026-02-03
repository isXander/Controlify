package dev.isxander.controlify.compatibility.yacl.mixins;

import dev.isxander.yacl3.gui.OptionListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(OptionListWidget.GroupSeparatorEntry.class)
public interface GroupSeparatorEntryAccessor {
    @Accessor
    List<OptionListWidget.Entry> getChildEntries();
}
