package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import dev.isxander.controlify.gui.ButtonGuideRenderer;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.CreateWorldScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin implements ScreenProcessorProvider {
    @Shadow protected abstract void onCreate();

    @Unique private final ScreenProcessor<CreateWorldScreen> processor = new CreateWorldScreenProcessor((CreateWorldScreen) (Object) this, this::onCreate);

    @ModifyArg(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 1))
    private LayoutElement modifyCancelButton(LayoutElement button) {
        ButtonGuideRenderer.registerBindingForButton((AbstractButton) button, bindings -> bindings.GUI_BACK, ButtonRenderPosition.TEXT, ButtonGuidePredicate.ALWAYS);
        return button;
    }

    @ModifyArg(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 0))
    private LayoutElement modifyCreateButton(LayoutElement button) {
        ButtonGuideRenderer.registerBindingForButton((AbstractButton) button, bindings -> bindings.GUI_ABSTRACT_ACTION_1, ButtonRenderPosition.TEXT, ButtonGuidePredicate.ALWAYS);
        return button;
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }
}
