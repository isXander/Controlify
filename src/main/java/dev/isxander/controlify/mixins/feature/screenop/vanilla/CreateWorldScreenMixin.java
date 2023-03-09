package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.CreateWorldScreenProcessor;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin implements ScreenProcessorProvider {
    @Shadow private @Nullable TabNavigationBar tabNavigationBar;
    @Shadow @Final private TabManager tabManager;

    @Unique private final CreateWorldScreenProcessor controlify$screenProcessor
            = new CreateWorldScreenProcessor((CreateWorldScreen) (Object) this, this::changeTab);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return controlify$screenProcessor;
    }

    private void changeTab(boolean reverse) {
        List<Tab> tabs = ((TabNavigationBarAccessor) tabNavigationBar).getTabs();
        int currentIndex = tabs.indexOf(tabManager.getCurrentTab());

        int newIndex = currentIndex + (reverse ? -1 : 1);
        if (newIndex < 0) newIndex = tabs.size() - 1;
        if (newIndex >= tabs.size()) newIndex = 0;

        tabNavigationBar.selectTab(newIndex, true);
    }
}
