package dev.isxander.controlify.mixins.feature.screenop.impl.container;

import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.CreativeModeInventoryScreenProcessor;
import dev.isxander.controlify.screenop.compat.vanilla.EditBoxComponentProcessor;
import dev.isxander.controlify.screenop.keyboard.InputTarget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreenMixin implements ScreenProcessorProvider {
    @Shadow
    private EditBox searchBox;

    @Shadow
    protected abstract void refreshSearchResults();

    @Unique
    protected CreativeModeInventoryScreenProcessor screenProcessor = new CreativeModeInventoryScreenProcessor(
            (CreativeModeInventoryScreen) (Object) this,
            () -> hoveredSlot,
            this::slotClicked,
            this::handleControllerItemSlotActions
    );

    @Inject(method = "init", at = @At("RETURN"))
    private void fixSearchUpdate(CallbackInfo ci) {
        if (this.searchBox != null) {
            var processor = (EditBoxComponentProcessor) ComponentProcessorProvider.provide(this.searchBox);
            processor.setInputTarget(new InputTarget.Delegated(new EditBoxComponentProcessor.EditBoxInputTarget(this.searchBox)) {
                @Override
                public boolean acceptChar(char ch, int modifiers) {
                    CreativeModeInventoryScreenMixin.this.refreshSearchResults();
                    return super.acceptChar(ch, modifiers);
                }

                @Override
                public boolean acceptKeyCode(int keycode, int scancode, int modifiers) {
                    CreativeModeInventoryScreenMixin.this.refreshSearchResults();
                    return super.acceptKeyCode(keycode, scancode, modifiers);
                }
            });
        }
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return screenProcessor;
    }
}
