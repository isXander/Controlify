//? if sodium {
package dev.isxander.controlify.compatibility.sodium.mixins;

import dev.isxander.controlify.compatibility.sodium.screenop.SodiumGuiScreenProcessor;
import dev.isxander.controlify.compatibility.sodium.screenop.SodiumScreenOperations;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.structure.Page;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.OptionListWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends Screen implements ScreenProcessorProvider, SodiumScreenOperations {
    @Shadow private FlatButtonWidget applyButton;
    @Shadow private FlatButtonWidget closeButton;
    @Shadow private FlatButtonWidget undoButton;
    @Shadow private OptionListWidget optionList;

    @Unique private Page controlify$currentPage = null;
    @Unique private final SodiumGuiScreenProcessor controlify$screenProcessor
            = new SodiumGuiScreenProcessor((VideoSettingsScreen) (Object) this, this);

    protected VideoSettingsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void controlify$onInit(CallbackInfo ci) {
        if (optionList != null && !optionList.getControls().isEmpty()) {
            this.setInitialFocus(optionList.getControls().get(0));
        }
        controlify$screenProcessor.onRebuildGUI();
    }

    @Inject(method = "onSectionFocused", at = @At("HEAD"))
    private void controlify$trackCurrentPage(Page page, CallbackInfo ci) {
        controlify$currentPage = page;
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return controlify$screenProcessor;
    }

    @Override
    public void controlify$nextPage() {
        List<Page> pages = controlify$getAllPages();
        if (pages.isEmpty()) return;
        int currentIndex = controlify$currentPage != null ? pages.indexOf(controlify$currentPage) : -1;
        int nextIndex = (currentIndex + 1) % pages.size();
        jumpToPage(pages.get(nextIndex));
    }

    @Override
    public void controlify$prevPage() {
        List<Page> pages = controlify$getAllPages();
        if (pages.isEmpty()) return;
        int currentIndex = controlify$currentPage != null ? pages.indexOf(controlify$currentPage) : 0;
        int nextIndex = (currentIndex - 1 + pages.size()) % pages.size();
        jumpToPage(pages.get(nextIndex));
    }

    @Unique
    private List<Page> controlify$getAllPages() {
        return ConfigManager.CONFIG.getModOptions().stream()
                .flatMap(mod -> mod.pages().stream())
                .toList();
    }

    @Shadow public abstract void jumpToPage(Page page);

    @Override
    public FlatButtonWidget controlify$getApplyButton() {
        return applyButton;
    }

    @Override
    public FlatButtonWidget controlify$getCloseButton() {
        return closeButton;
    }

    @Override
    public FlatButtonWidget controlify$getUndoButton() {
        return undoButton;
    }
}
//?}
