package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TabNavigationBar.class)
public interface TabNavigationBarAccessor {
    @Accessor
    ImmutableList<Tab> getTabs();
}
