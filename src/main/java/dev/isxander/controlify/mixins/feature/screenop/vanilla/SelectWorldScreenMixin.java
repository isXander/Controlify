package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.SelectWorldScreenProcessor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin implements ScreenProcessorProvider {
    @Unique
    private final SelectWorldScreenProcessor processor = new SelectWorldScreenProcessor((SelectWorldScreen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }

    @ModifyExpressionValue(
            method = "init()V",
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            //? if >1.20.1 {
                            target = "Lnet/minecraft/network/chat/CommonComponents;GUI_BACK:Lnet/minecraft/network/chat/Component;",
                            //?} else {
                            /*target = "Lnet/minecraft/network/chat/CommonComponents;GUI_CANCEL:Lnet/minecraft/network/chat/Component;",
                            *///?}
                            opcode = Opcodes.GETSTATIC,
                            ordinal = 0
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;",
                    ordinal = 0
            )
    )
    private Button modifyCancelButton(Button button) {
        ButtonGuideApi.addGuideToButton(
                button,
                ControlifyBindings.GUI_BACK,
                ButtonGuidePredicate.always()
        );
        return button;
    }

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
                    target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;",
                    ordinal = 0
            )
    )
    private Button modifyCreateButton(Button button) {
        ButtonGuideApi.addGuideToButton(
                button,
                ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                ButtonGuidePredicate.always()
        );
        return button;
    }
}
