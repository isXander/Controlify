package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.CreateWorldScreenProcessor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin implements ScreenProcessorProvider {
    @Shadow protected abstract void onCreate();

    @Unique private final ScreenProcessor<CreateWorldScreen> processor
            = new CreateWorldScreenProcessor((CreateWorldScreen) (Object) this, this::onCreate);

    @ModifyExpressionValue(
            method = "init()V",
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=selectWorld.create"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;"
            )
    )
    private Button modifyCreateButton(Button button) {
        ButtonGuideApi.addGuideToButtonBuiltin(
                button,
                bindings -> bindings.GUI_ABSTRACT_ACTION_1,
                ButtonGuidePredicate.ALWAYS
        );
        return button;
    }

    @ModifyExpressionValue(
            method = "init()V",
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/network/chat/CommonComponents;GUI_CANCEL:Lnet/minecraft/network/chat/Component;"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;"
            )
    )
    private Button modifyCancelButton(Button button) {
        ButtonGuideApi.addGuideToButtonBuiltin(
                button,
                bindings -> bindings.GUI_BACK,
                ButtonGuidePredicate.ALWAYS
        );
        return button;
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }
}
