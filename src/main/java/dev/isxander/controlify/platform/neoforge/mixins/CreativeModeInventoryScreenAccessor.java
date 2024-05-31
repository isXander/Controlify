package dev.isxander.controlify.platform.neoforge.mixins;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeModeInventoryScreenAccessor {
    @Accessor
    static CreativeModeTab getSelectedTab() {
        throw new AssertionError();
    }

    @Invoker
    void invokeSelectTab(CreativeModeTab tab);

    @Accessor
    List<CreativeTabsScreenPage> getPages();
}
