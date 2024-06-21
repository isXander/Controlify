package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.SelectWorldScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin implements ScreenProcessorProvider {
    @Unique
    private final SelectWorldScreenProcessor processor = new SelectWorldScreenProcessor((SelectWorldScreen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }

    @ModifyArg(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/SelectWorldScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 5))
    private <T extends GuiEventListener & Renderable> T modifyCancelButton(T button) {
        ButtonGuideApi.addGuideToButton(
                (AbstractButton) button,
                ControlifyBindings.GUI_BACK,
                ButtonGuidePredicate.always()
        );
        return button;
    }

    @ModifyArg(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/SelectWorldScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 1))
    private <T extends GuiEventListener & Renderable> T modifyCreateButton(T button) {
        ButtonGuideApi.addGuideToButton(
                (AbstractButton) button,
                ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                ButtonGuidePredicate.always()
        );
        return button;
    }
}
