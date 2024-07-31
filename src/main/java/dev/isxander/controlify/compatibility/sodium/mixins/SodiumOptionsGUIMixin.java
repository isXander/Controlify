/*? if sodium {*/
package dev.isxander.controlify.compatibility.sodium.mixins;

import dev.isxander.controlify.compatibility.sodium.screenop.SodiumGuiScreenProcessor;
import dev.isxander.controlify.compatibility.sodium.screenop.SodiumScreenOperations;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlElement;
import me.jellysquid.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = SodiumOptionsGUI.class, remap = false)
public abstract class SodiumOptionsGUIMixin extends Screen implements ScreenProcessorProvider, SodiumScreenOperations {
    @Shadow @Final private List<ControlElement<?>> controls;

    @Shadow
    @Final
    private List<OptionPage> pages;
    @Shadow
    private OptionPage currentPage;

    @Shadow
    public abstract void setPage(OptionPage page);

    @Shadow
    private FlatButtonWidget applyButton;
    @Shadow
    private FlatButtonWidget closeButton;
    @Shadow
    private FlatButtonWidget undoButton;
    @Unique private final SodiumGuiScreenProcessor controlify$screenProcessor
            = new SodiumGuiScreenProcessor((SodiumOptionsGUI) (Object) this, this);

    protected SodiumOptionsGUIMixin(Component title) {
        super(title);
    }

    @Inject(method = "rebuildGUIOptions", at = @At("RETURN"))
    private void focusFirstButton(CallbackInfo ci) {
        this.setInitialFocus(controls.get(0));
    }

    @Inject(method = "rebuildGUI", at = @At("RETURN"))
    private void notifyScreenProcessorOfRebuild(CallbackInfo ci) {
        controlify$screenProcessor.onRebuildGUI();
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return controlify$screenProcessor;
    }

    @Override
    public void controlify$nextPage() {
        var currentIndex = pages.indexOf(currentPage);
        var nextIndex = (currentIndex + 1) % pages.size();
        setPage(pages.get(nextIndex));
    }

    @Override
    public void controlify$prevPage() {
        var currentIndex = pages.indexOf(currentPage);
        var nextIndex = (currentIndex - 1 + pages.size()) % pages.size();
        setPage(pages.get(nextIndex));
    }

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
/*?}*/
