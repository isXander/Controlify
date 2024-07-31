//? if reeses-sodium-options {
package dev.isxander.controlify.compatibility.rso.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.compatibility.sodium.screenop.SodiumGuiScreenProcessor;
import dev.isxander.controlify.compatibility.sodium.screenop.SodiumScreenOperations;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import me.flashyreese.mods.reeses_sodium_options.client.gui.SodiumVideoOptionsScreen;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.AbstractFrame;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.tab.Tab;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.tab.TabFrame;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlElement;
import me.jellysquid.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = SodiumVideoOptionsScreen.class, remap = false)
public abstract class SodiumVideoOptionsScreenMixin extends Screen implements ScreenProcessorProvider, SodiumScreenOperations {
    @Shadow
    private FlatButtonWidget applyButton;
    @Shadow
    private FlatButtonWidget closeButton;
    @Shadow
    private FlatButtonWidget undoButton;

    @Unique
    private final SodiumGuiScreenProcessor controlify$screenProcessor
            = new SodiumGuiScreenProcessor((SodiumVideoOptionsScreen) (Object) this, this);

    @Unique
    private TabFrame tabFrame;

    protected SodiumVideoOptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void notifyProcessorRebuild(CallbackInfo ci) {
        controlify$screenProcessor.onRebuildGUI();
        focusOnFirstControl();
    }

    @ModifyExpressionValue(
            method = "lambda$parentBasicFrameBuilder$9",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/flashyreese/mods/reeses_sodium_options/client/gui/frame/tab/TabFrame$Builder;build()Lme/flashyreese/mods/reeses_sodium_options/client/gui/frame/tab/TabFrame;"
            )
    )
    private TabFrame storeBuiltTabFrame(TabFrame original) {
        return tabFrame = original;
    }

    @ModifyArg(
            method = "lambda$parentBasicFrameBuilder$9",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/flashyreese/mods/reeses_sodium_options/client/gui/frame/tab/TabFrame$Builder;onSetTab(Ljava/lang/Runnable;)Lme/flashyreese/mods/reeses_sodium_options/client/gui/frame/tab/TabFrame$Builder;"
            )
    )
    private Runnable setInitialFocusOnTabChange(Runnable onSetTab) {
        return () -> {
            onSetTab.run();
            Minecraft.getInstance().tell(this::focusOnFirstControl);
        };
    }

    @Unique
    private void focusOnFirstControl() {
        AbstractFrame tabContentsFrame = ((TabFrameAccessor) tabFrame).getSelectedFrame();
        List<ControlElement<?>> controlElements = ((AbstractFrameAccessor) tabContentsFrame).getControlElements();
        if (!controlElements.isEmpty()) {
            System.out.println(controlElements.get(0).getOption().getName().getString());
            setInitialFocus(controlElements.get(0));
        }
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return controlify$screenProcessor;
    }

    @Override
    public void controlify$nextPage() {
        var accessor = (TabFrameAccessor) this.tabFrame;
        List<Tab<?>> tabs = accessor.getTabs();

        var currentIndex = tabs.indexOf(accessor.getSelectedTab());
        var nextIndex = (currentIndex + 1) % tabs.size();
        tabFrame.setTab(tabs.get(nextIndex));
    }

    @Override
    public void controlify$prevPage() {
        var accessor = (TabFrameAccessor) this.tabFrame;
        List<Tab<?>> tabs = accessor.getTabs();

        var currentIndex = tabs.indexOf(accessor.getSelectedTab());
        var nextIndex = (currentIndex - 1 + tabs.size()) % tabs.size();
        tabFrame.setTab(tabs.get(nextIndex));
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
//?}
